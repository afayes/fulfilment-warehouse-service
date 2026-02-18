# Questions

Here we have 3 questions related to the code base for you to answer. It is not about right or wrong, but more about what's the reasoning behind your decisions.

1. In this code base, we have some different implementation strategies when it comes to database access layer and manipulation. If you would maintain this code base, would you refactor any of those? Why?

**Answer:**
```txt
The codebase uses four different database access strategies:

- Stores: Active Record pattern (Store extends PanacheEntity, static methods like Store.findById())
- Products: Repository pattern (plain @Entity + injected PanacheRepository)
- Warehouses: Hexagonal architecture (plain @Entity + PanacheRepository implementing a domain port interface, with a separate domain model)
- Fulfilment: Repository + Service layer (plain @Entity + PanacheRepository + service encapsulating business logic)

I would refactor towards a consistent approach. Specifically:

1. Move Stores away from Active Record to the Repository pattern. Active Record couples the entity to persistence, making unit testing harder — you cannot mock static calls like Store.findById() without framework support. A StoreRepository would align with Products and Fulfilment, improving testability and consistency.

2. Adopt the Warehouses' hexagonal approach as the target architecture for all domains. It provides the cleanest separation: JPA entities stay in the adapter layer, domain models are persistence-agnostic, and business logic depends only on port interfaces. This makes use cases independently testable with simple mocks.

3. As a pragmatic middle ground, at minimum ensure every domain uses the Repository pattern (not Active Record) and has a service/use-case layer for business logic. The full hexagonal structure (separate domain models, port interfaces) adds value for complex domains like Warehouses but may be over-engineering for simple CRUD domains like Products. The key principle is: consistency within the codebase, with complexity proportional to the domain.
```
----
2. When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded directly everything. What would be your thoughts about what are the pros and cons of each approach and what would be your choice?

**Answer:**
```txt
OpenAPI-first (Warehouse approach):

Pros:
- Contract-driven: the API spec is the single source of truth, ensuring consistency between documentation, client expectations, and implementation.
- Generated interfaces enforce that the implementation matches the spec at compile time — you cannot accidentally drift from the contract.
- Enables parallel work: frontend/consumer teams can code against the spec before the backend is complete.
- Auto-generated documentation stays in sync with the code.

Cons:
- Less flexibility — changes require updating the YAML first, regenerating code, then adapting the implementation. This adds friction for rapid iteration.
- Generated code can impose constraints (e.g. return types, status codes) that are harder to override. In this codebase, the generated Warehouse interface returns void/entity directly, making it difficult to return custom HTTP status codes like 201 for POST.
- Requires tooling setup (OpenAPI generator plugin, build configuration) and the team must understand the generation pipeline.

Code-first (Store/Product approach):

Pros:
- Fast and simple — define the endpoint directly in JAX-RS with full control over annotations, return types, and status codes.
- No build tooling overhead; easy to understand and modify.
- Better suited for internal APIs or rapid prototyping where the contract evolves frequently.

Cons:
- No compile-time guarantee that the API matches documentation. The spec and code can drift apart silently.
- Documentation must be maintained separately or generated post-hoc (e.g. via SmallRye OpenAPI annotations).

My choice:
For a team-based product with external consumers, I would choose OpenAPI-first. The contract enforcement and documentation benefits outweigh the friction, especially as the API stabilises. For internal services or early-stage prototyping, code-first with OpenAPI annotations (generating the spec from code) is a pragmatic alternative that still produces a spec without the generation pipeline overhead.
```
----
3. Given the need to balance thorough testing with time and resource constraints, how would you prioritize and implement tests for this project? Which types of tests would you focus on, and how would you ensure test coverage remains effective over time?

**Answer:**
```txt
I would follow the test pyramid model: a broad base of fast unit tests, a middle layer of integration tests, and a thin top of end-to-end smoke tests. This maximises confidence while keeping the test suite fast and maintainable.

Priority 1 — Unit tests for business logic (broad base, highest value per effort):
Focus on domain rules and validations: warehouse creation/replacement constraints, fulfilment association limits, and edge cases around those boundaries. These tests are fast, isolated (using mocks), and catch the most impactful bugs — incorrect business rules. Examples: CreateWarehouseUseCaseTest, ReplaceWarehouseUseCaseTest, FulfilmentServiceTest. This is where I would invest most effort.

Priority 2 — Integration tests for REST endpoints (middle layer, @QuarkusTest):
Verify the full request-response cycle: correct status codes, error mapping, request validation, and that the layers (resource → service → repository → database) work together. These catch wiring issues, serialisation bugs, and exception-to-HTTP-status mapping errors. Examples: WarehouseEndpointTest, FulfilmentResourceTest. One happy-path and key error-path test per endpoint provides high confidence without excessive duplication of unit-level logic.

Priority 3 — Smoke tests (thin top, @QuarkusIntegrationTest):
A small number of end-to-end tests against the packaged application to verify it starts correctly and core flows work. These catch packaging, configuration, and runtime issues that @QuarkusTest misses. Keep these minimal — they are slow and brittle. Examples: WarehouseEndpointIT with a few key happy-path scenarios.

Keeping coverage effective over time:
- Run tests in CI on every push — fast unit tests gate merges, slower integration tests run in parallel.
- Follow consistent naming conventions (e.g. methodName_shouldExpectedBehaviour_whenCondition) so test intent is immediately clear.
- Treat test code with the same quality standards as production code: use helper methods to reduce duplication, clean up test data, and keep tests independent.
- When fixing a bug, always add a regression test first (as I did with the StoreResource.patch() fix) — this prevents the same defect from recurring and naturally grows coverage over time.
- Enforce a minimum code coverage threshold (e.g. 80% line coverage) via a build plugin like JaCoCo, configured to fail the build if coverage drops below the threshold. This acts as a safety net — it doesn't guarantee good tests, but it prevents large untested areas from creeping in. Focus the threshold on business logic packages rather than generated code or simple DTOs.
```

----
### Additional note: Bug fix in `StoreResource.patch()`

While writing tests for the PATCH endpoint, I identified a bug in `StoreResource.patch()` where the partial update conditions were checking the existing entity's fields instead of the incoming request fields. This meant, for example, that a store with zero stock could never have its stock updated via PATCH.

**Before:**
```java
if (entity.name != null) {
    entity.name = updatedStore.name;
}

if (entity.quantityProductsInStock != 0) {
    entity.quantityProductsInStock = updatedStore.quantityProductsInStock;
}
```

**After:**
```java
if (updatedStore.name != null) {
    entity.name = updatedStore.name;
}

if (updatedStore.quantityProductsInStock != 0) {
    entity.quantityProductsInStock = updatedStore.quantityProductsInStock;
}
```

The fix checks the incoming request fields, which is the correct semantics for a partial update. Added a corresponding test (`patch_shouldUpdateOnlyName_whenStockIsNotProvided`) to verify the fix.
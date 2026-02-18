## About the assignment

You will find the tasks of this assignment on [CODE_ASSIGNMENT](assignment/CODE_ASSIGNMENT.md) file

## About the code base

Some of this code here is based on https://github.com/quarkusio/quarkus-quickstarts

## Implementation Notes

### Completed Tasks
- **Location**: Implemented `LocationGateway.resolveByIdentifier`
- **Store**: Fixed `LegacyStoreManagerGateway` to propagate changes only after DB commit
- **Warehouse**: Full CRUD with hexagonal architecture (use cases, ports, adapters), including create/replace/archive validations
- **Fulfilment (Bonus)**: Warehouse-Product-Store associations with all three constraints enforced via a service layer

### Design Approach
- Followed **layered architecture** with clear separation: REST resources (thin adapters) → service/use-case layer (business logic) → repository/port layer (persistence)
- Applied **SOLID principles** throughout — single responsibility per class, dependency inversion via port interfaces (e.g. `WarehouseStore`, `LocationResolver`), and open/closed through the use-case pattern
- Custom domain exceptions (`WarehouseValidationException`, `FulfilmentNotFoundException`) for precise error handling across layers

### Additional Improvements
- **Bug fix**: Fixed `StoreResource.patch()` checking wrong fields for partial update (see [QUESTIONS.md](java-assignment/QUESTIONS.md) for details)
- **Test coverage**: 76 tests across unit, integration (`@QuarkusTest`), and smoke (`@QuarkusIntegrationTest`) layers; JaCoCo integrated via `quarkus-jacoco` — run `./mvnw test` from `java-assignment/` and view the report at `target/jacoco-report/index.html`
- **CI**: GitHub Actions workflow runs build and tests on push
- **Logging**: Structured logging at resource and service layers using `org.jboss.logging.Logger`

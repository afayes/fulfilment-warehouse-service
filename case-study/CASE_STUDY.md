# Case Study Scenarios to discuss

## Scenario 1: Cost Allocation and Tracking
**Situation**: The company needs to track and allocate costs accurately across different Warehouses and Stores. The costs include labor, inventory, transportation, and overhead expenses.

**Task**: Discuss the challenges in accurately tracking and allocating costs in a fulfillment environment. Think about what are important considerations for this, what are previous experiences that you have you could related to this problem and elaborate some questions and considerations

**Questions you may have and considerations:**

**Key challenges:**

1. **Shared cost attribution**: Many fulfilment costs are shared across multiple warehouses, stores, or products. Transportation costs may serve multiple stores in a single route; warehouse overhead (rent, utilities) benefits all products stored there. Deciding how to split these fairly — by volume, weight, order count, or revenue — significantly impacts which operations appear profitable.

2. **Granularity vs. practicality**: Tracking costs at a very granular level (per-item, per-order) provides the most accurate picture but increases system complexity and data volume. Too coarse (per-warehouse monthly totals) hides inefficiencies. Finding the right level of granularity is a key design decision.

3. **Timeliness of data**: Some costs are known in real-time (e.g. labour hours logged), while others arrive with delay (e.g. utility bills, carrier invoices). The system must handle both real-time and retroactive cost entries without distorting reports.

4. **Cost categories vary by context**: Labour costs in a warehouse (picking, packing) are fundamentally different from transportation costs or store-level overhead. A one-size-fits-all model risks oversimplifying — the system needs flexible cost categorisation.

**Questions I would ask before scoping:**

- What cost categories does the business currently track, and which are the highest priority to get right first? (Helps define MVP scope.)
- How are costs allocated today — spreadsheets, an ERP system, manual processes? (Understand the current baseline and pain points.)
- Who are the consumers of this data — finance teams, warehouse managers, store managers? What decisions do they make with it? (Drives reporting granularity and access requirements.)
- Are there existing cost centre codes or accounting structures we need to align with? (Integration and data model constraints.)
- What is the expected volume — how many warehouses, stores, and cost entries per month? (Informs architectural decisions: batch vs. real-time, storage strategy.)
- How should shared costs (e.g. a delivery route serving 3 stores) be apportioned? Is there an existing business rule, or does this need to be defined? (Critical business logic that must be agreed before building.)
- What level of historical data needs to be preserved, especially when warehouses are replaced/archived? (Relates directly to the "replace" operation in the domain — archived warehouses must retain their cost history.)

## Scenario 2: Cost Optimization Strategies
**Situation**: The company wants to identify and implement cost optimization strategies for its fulfillment operations. The goal is to reduce overall costs without compromising service quality.

**Task**: Discuss potential cost optimization strategies for fulfillment operations and expected outcomes from that. How would you identify, prioritize and implement these strategies?

**Questions you may have and considerations:**

**Potential strategies and expected outcomes:**

1. **Warehouse consolidation and fulfilment routing**: Analyse which warehouses serve overlapping store catchments. If two warehouses fulfil the same stores, consolidating stock into fewer, better-utilised warehouses reduces fixed overhead (rent, staffing baselines) while maintaining delivery capability. Expected outcome: lower per-unit warehousing cost, higher capacity utilisation.

2. **Inventory placement optimisation**: Position high-demand products closer to the stores that sell them most. This reduces transportation frequency and distance, which is typically the most variable and controllable cost. Expected outcome: reduced transportation spend, faster replenishment.

3. **Labour efficiency through demand-based staffing**: Align warehouse staffing levels with order volume patterns (seasonal peaks, day-of-week trends) rather than fixed headcount. Expected outcome: reduced idle labour cost without impacting throughput during peak periods.

4. **Supplier and carrier consolidation**: Negotiate volume-based rates by consolidating shipments or reducing the number of carriers. Fewer, larger shipments are typically cheaper per unit than frequent small ones. Expected outcome: lower per-unit transportation cost.

**How to identify, prioritise, and implement:**

- **Identify**: Start with the cost data from Scenario 1. The largest cost categories with the highest variance between warehouses/stores represent the biggest optimisation opportunities. Look for outliers — a warehouse with significantly higher cost-per-unit than peers signals inefficiency.

- **Prioritise**: Rank strategies by (estimated saving) x (feasibility) / (implementation effort). Quick wins with low risk go first (e.g. route optimisation), structural changes with higher risk later (e.g. warehouse consolidation). Involve business stakeholders in prioritisation — a technically optimal change may be operationally disruptive.

- **Implement incrementally**: Roll out changes to a subset of warehouses/stores first, measure the actual impact against the baseline, then expand. This reduces risk and builds confidence with stakeholders. The Cost Control Tool should support A/B comparison — tracking costs before and after a strategy is applied to a specific warehouse.

**Questions I would ask before scoping:**

- What are the current top 3 cost drivers by category? (Focus effort where the money is.)
- Are there existing SLAs or service commitments to stores (e.g. delivery within 24 hours) that constrain which optimisations are feasible? (Optimising cost must not break service quality.)
- Is there flexibility to change warehouse-to-store assignments, or are these fixed by organisational structure? (Determines whether routing optimisation is in scope.)
- What data is currently available to measure cost per unit, per warehouse, per route? (If we cannot measure it, we cannot optimise it — data gaps must be addressed first.)
- How does the business define "service quality" — delivery speed, stock availability, order accuracy? (Ensures optimisation targets do not conflict with quality metrics.)

## Scenario 3: Integration with Financial Systems
**Situation**: The Cost Control Tool needs to integrate with existing financial systems to ensure accurate and timely cost data. The integration should support real-time data synchronization and reporting.

**Task**: Discuss the importance of integrating the Cost Control Tool with financial systems. What benefits the company would have from that and how would you ensure seamless integration and data synchronization?

**Questions you may have and considerations:**

**Why integration matters:**

A Cost Control Tool operating in isolation from financial systems creates a dual-source-of-truth problem. Teams end up reconciling numbers manually between the operational tool and the financial ledger, which is slow, error-prone, and erodes trust in both systems. Integration ensures that cost data flows consistently — operational costs captured at the warehouse/store level are reflected accurately in the company's financial reporting, and financial budgets feed back into operational cost targets.

**Key benefits:**

1. **Single source of truth**: Eliminates discrepancies between operational cost tracking and financial reporting. When a warehouse manager sees the same numbers as the finance team, decision-making is faster and more aligned.

2. **Timely visibility**: Real-time or near-real-time synchronisation means cost overruns are detected as they happen, not weeks later when an invoice is reconciled. This enables proactive corrective action.

3. **Reduced manual effort**: Automated data flow removes the need for manual export/import cycles, freeing finance and operations teams to focus on analysis rather than data entry.

4. **Audit and compliance**: A connected system provides a clear data lineage from operational event to financial ledger entry, supporting audit requirements and regulatory compliance.

**Ensuring seamless integration:**

- **Event-driven architecture**: Publish cost events (e.g. "cost recorded", "cost adjusted") from the Cost Control Tool and let the financial system subscribe. This decouples the systems — each can evolve independently, and temporary downtime on one side does not block the other. A message broker (e.g. Kafka, RabbitMQ) provides durability and retry semantics.

- **Canonical data model**: Define a shared schema for cost records (cost centre, category, amount, currency, timestamp, warehouse/store reference). Both systems map to/from this model, avoiding tight coupling to either system's internal structure.

- **Idempotent operations**: Financial entries must not be duplicated if a message is retried. Each cost event should carry a unique identifier so the receiving system can safely deduplicate.

- **Reconciliation process**: Even with real-time sync, build a periodic reconciliation job that compares totals between the two systems and flags discrepancies. No integration is perfect — a safety net catches edge cases.

**Questions I would ask before scoping:**

- Which financial systems are in use (SAP, Oracle Financials, a custom ERP)? What integration APIs or protocols do they support? (Determines technical approach — REST API, file-based, messaging.)
- What is the acceptable latency for cost data to appear in the financial system — real-time, hourly, daily? (Drives architecture: event-driven vs. batch sync.)
- Who owns the financial system integration today, and what governance exists around changes to it? (Organisational constraint — we may need to work with a separate team.)
- Are there existing data pipelines or an integration platform (e.g. MuleSoft, Apache Camel) we should leverage rather than build from scratch?
- How are currency and tax handled — does the Cost Control Tool need to manage these, or does the financial system handle conversion? (Scope boundary.)
- What happens when the financial system is unavailable — should the Cost Control Tool queue entries, or is data loss acceptable for a retry window? (Resilience requirements.)

## Scenario 4: Budgeting and Forecasting
**Situation**: The company needs to develop budgeting and forecasting capabilities for its fulfillment operations. The goal is to predict future costs and allocate resources effectively.

**Task**: Discuss the importance of budgeting and forecasting in fulfillment operations and what would you take into account designing a system to support accurate budgeting and forecasting?

**Questions you may have and considerations:**

**Why budgeting and forecasting matter in fulfilment:**

Fulfilment costs are highly variable — they shift with seasonal demand, new store openings, warehouse replacements, and supplier price changes. Without forecasting, the business operates reactively: overspending is only visible after the fact. Accurate budgets give warehouse and store managers clear cost targets, while forecasts enable proactive resource allocation — hiring temporary staff before a peak season rather than scrambling during it.

**Design considerations for a budgeting and forecasting system:**

1. **Historical data as the foundation**: Forecasting accuracy depends on the quality and depth of historical cost data. The system must store cost records at sufficient granularity (per warehouse, per category, per period) to identify trends and seasonal patterns. This ties directly to Scenario 1 — the cost tracking system feeds the forecasting model.

2. **Seasonality and demand patterns**: Fulfilment costs are not linear. Holiday peaks, promotional events, and seasonal product ranges create predictable spikes. The system should support time-series analysis that accounts for these recurring patterns rather than simple linear extrapolation.

3. **Scenario modelling**: The business needs to answer "what if" questions — what if we open a new warehouse, replace an existing one, add a product line, or lose a carrier contract? The system should allow users to create budget scenarios with adjustable parameters (volume, unit costs, capacity) and compare projected outcomes.

4. **Budget vs. actual tracking**: A budget is only useful if it is continuously compared to actuals. The system should provide real-time dashboards showing variance (budget minus actual) per warehouse, per store, per cost category, with alerts when variance exceeds a configurable threshold. This closes the feedback loop between planning and execution.

5. **Granularity of budgets**: Budgets should align with operational responsibility. A warehouse manager should see and own their warehouse's budget, not the company-wide total. This means budgets need to be structured per cost centre (warehouse, store, route) and aggregated upward for leadership reporting.

6. **Rolling forecasts over fixed annual budgets**: A fixed annual budget becomes stale quickly in a dynamic fulfilment environment. Consider supporting rolling forecasts (e.g. always looking 12 months ahead, updated monthly) that incorporate the latest actuals and adjust projections accordingly.

**Questions I would ask before scoping:**

- How many months/years of historical cost data are available, and at what granularity? (Determines whether we can forecast immediately or need a data collection phase first.)
- Does the business currently use fixed annual budgets, rolling forecasts, or neither? (Understand the current process to design an improvement, not a replacement that nobody adopts.)
- Who creates budgets — central finance, or individual warehouse/store managers? Who approves them? (Drives workflow and access control design.)
- What planning cycles exist — annual, quarterly, monthly? (The system's update cadence must match the business rhythm.)
- Are there known upcoming changes — new warehouses, store closures, geographic expansion — that need to be modelled in the first version? (Helps define initial scenario modelling scope.)
- What forecasting sophistication is expected — simple trend-based projections, or machine-learning-driven models? (Sets expectations and determines whether we build or buy the forecasting engine.)

## Scenario 5: Cost Control in Warehouse Replacement
**Situation**: The company is planning to replace an existing Warehouse with a new one. The new Warehouse will reuse the Business Unit Code of the old Warehouse. The old Warehouse will be archived, but its cost history must be preserved.

**Task**: Discuss the cost control aspects of replacing a Warehouse. Why is it important to preserve cost history and how this relates to keeping the new Warehouse operation within budget?

**Questions you may have and considerations:**

**Why preserving cost history matters:**

When a warehouse is replaced, the Business Unit Code is reused — the new warehouse inherits the identity of the old one. Without preserved cost history, the business loses the ability to distinguish between the old warehouse's costs and the new one's. This creates several problems:

1. **Baseline for the new warehouse**: The archived warehouse's cost history provides the benchmark. Was the replacement justified? Is the new warehouse performing better, worse, or as expected compared to its predecessor? Without the old data, there is no basis for comparison.

2. **Accurate trend analysis**: If old and new costs are merged under the same Business Unit Code without separation, historical reports become misleading — a sudden cost drop or spike at the replacement date would distort trend lines and forecasts (Scenario 4).

3. **Audit and accountability**: Financial audits may require tracing costs back to the specific warehouse that incurred them. The archived warehouse's costs belong to a different legal/operational period and must remain attributable to it.

4. **Transition cost visibility**: The replacement itself incurs costs — decommissioning the old warehouse, setting up the new one, moving inventory, temporary dual-operation. These transition costs must be captured separately so they do not inflate the new warehouse's ongoing operational baseline.

**Cost control aspects of the replacement:**

- **Budget the replacement as a project**: Treat the transition as a time-bounded project with its own budget (setup costs, migration, parallel running). This prevents transition costs from being absorbed silently into operational budgets.

- **Set a cost target for the new warehouse**: Use the archived warehouse's historical cost data to set a realistic budget for the new warehouse. If the replacement was motivated by cost reduction (e.g. a more efficient facility), the new warehouse's budget should reflect that expected improvement.

- **Monitor the ramp-up period**: New warehouses typically have higher costs initially (staff learning, process stabilisation). The system should support a defined ramp-up period where cost targets are relaxed, transitioning to full operational targets once steady state is reached.

- **Data model design**: The system should link old and new warehouse records via the shared Business Unit Code while keeping them as distinct entities with separate cost ledgers. The `archivedAt` timestamp on the old warehouse provides a natural partition point — costs before that date belong to the old warehouse, costs after belong to the new one.

**Questions I would ask before scoping:**

- How frequently do warehouse replacements occur? (Determines whether this is a one-off migration concern or a recurring operational pattern that needs robust tooling.)
- What costs are expected during the transition period — inventory transfer, dual staffing, setup fees? Is there an existing budget template for replacements? (Defines what "transition cost" means concretely.)
- Should the new warehouse's cost reports include a comparison view against the archived predecessor, or are they treated as fully independent? (Drives reporting requirements.)
- How long should the archived warehouse's cost data be retained — indefinitely, or is there a retention policy? (Storage and compliance consideration.)
- Are there cases where a Business Unit Code has been reused more than once (multiple replacements over time)? (Affects data model — may need to support a chain of warehouses, not just old/new pairs.)
- Who approves the budget for the new warehouse, and is it derived from the old warehouse's actuals or set independently? (Defines the workflow between cost history and budget setting.)

## Instructions for Candidates
Before starting the case study, read the [BRIEFING.md](BRIEFING.md) to quickly understand the domain, entities, business rules, and other relevant details.

**Analyze the Scenarios**: Carefully analyze each scenario and consider the tasks provided. To make informed decisions about the project's scope and ensure valuable outcomes, what key information would you seek to gather before defining the boundaries of the work? Your goal is to bridge technical aspects with business value, bringing a high level discussion; no need to deep dive.

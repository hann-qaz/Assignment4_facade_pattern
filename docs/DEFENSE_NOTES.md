# Defense Notes

## One minute explanation

The project models online order processing. A normal order needs five services in a strict sequence: reserve stock, charge payment, schedule shipping, save the order, and notify the customer. `OrderFacade` gives the client one `placeOrder` method and one `cancelOrder` method. It delegates real work to focused subsystem classes. If payment fails, it releases stock. If shipping fails, it refunds payment and releases stock. This is the Facade pattern because the purpose is to simplify a complex subsystem, not to change incompatible interfaces.

## Live demonstration

1. Run `mvn test` and show that all seven tests pass.
2. Run `mvn exec:java` and show order placement followed by cancellation.
3. Open `DemoClient` and point out that it imports no subsystem classes.
4. Open `OrderFacade` and explain the order of the calls and two rollback paths.
5. Open one subsystem, such as `InventoryService`, to show that it has no reference to `OrderFacade`.

## Likely questions

**Why is this a Facade and not an Adapter?**  
The interfaces already work together. The goal is to hide orchestration and give the client a smaller API. An Adapter would change an interface so incompatible code could use it.

**Does the Facade completely encapsulate the subsystem?**  
No. It provides simplified access. The public subsystem classes are still available to advanced clients, and none of them knows about the Facade.

**What does the Facade hide?**  
It hides object coordination, call order, payment and shipping identifiers, rollback, status storage, and notification selection.

**Why is rollback important?**  
Without rollback, a failed payment could leave stock unavailable, or a failed shipment could leave a customer charged. The Facade owns this coordination because no single subsystem has enough information to do it correctly.

**How do the tests follow F I R S T?**  
They are fast, independent, repeatable, self-validating, and written around each behavior. Every test creates fresh in-memory services and needs no network or database.

**Could OrderFacade become a god object?**  
Yes, if business rules were moved into it. Here it only coordinates. Stock checks, payment state, shipping state, notifications, and persistence stay in separate classes.

## Clean Code points

1. Meaningful names: `placeOrder`, `reserve`, `charge`, and `scheduleDelivery` explain intent.
2. Single Responsibility: the Facade coordinates while subsystem classes perform the work.
3. Small public API: clients receive `OrderResult`, not subsystem-specific payment or shipment objects.
4. Focused classes: each subsystem changes for one reason.
5. Error handling: known subsystem exceptions trigger explicit compensation steps.
6. Readable tests: each test name states one behavior and uses Arrange Act Assert structure.


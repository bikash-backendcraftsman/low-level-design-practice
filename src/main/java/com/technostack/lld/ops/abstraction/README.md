# Abstract Class vs Interface

In Low-Level Design and system design, choosing an abstract class or an interface is a design decision, not a syntax preference.

**Use an interface for CAN-DO** (a capability or a contract).  
**Use an abstract class for IS-A** (a core identity plus shared implementation).

An interface says what a type can do. An abstract class says what a type is, and it can already own fields, constructors, and code the subclasses reuse.

Java 8 and later let an interface have `default` and `private` methods. That does not replace an abstract class. An interface still cannot have instance fields, a constructor, or `protected` members.

---

## 1. When you need to share common state

An interface can hold only `public static final` constants. If related classes share mutable fields or protected data, use an abstract class.

- **System design concept:** base entity design in Spring Boot (JPA / Hibernate).
- **Example:** in RouteForge, almost every database entity needs an `id`, `createdAt`, and `updatedAt`.

```java
@MappedSuperclass
public abstract class BaseEntity {
    @Id
    @GeneratedValue
    private Long id;
    private LocalDateTime createdAt;

    protected boolean isExpired() {
        // shared state and behavior
        return false;
    }
}

public class Route extends BaseEntity { }
public class Vehicle extends BaseEntity { }
```

`Route` and `Vehicle` are both entities. The identity and the columns live once, in `BaseEntity`.

---

## 2. When you are implementing the Template Method pattern

Define the skeleton of an algorithm in the base class. Subclasses fill in specific steps and do not change the overall order.

- **System design concept:** one business workflow, several implementations of a single step.
- **Example:** processing a route request in RouteForge. The steps stay Validate, Calculate, Save, Notify. Only the calculation changes (Dijkstra vs A*).

```java
public abstract class AbstractRouteProcessor {
    // Template method. final so a subclass cannot reorder the steps.
    public final void processRoute(Request req) {
        validate(req);
        calculate(req);
        saveToDb(req);
        sendNotification(req);
    }

    private void validate(Request req) { /* shared logic */ }
    private void saveToDb(Request req) { /* shared logic */ }
    private void sendNotification(Request req) { /* shared logic */ }

    protected abstract void calculate(Request req);
}
```

The abstract class is the right tool here because the fixed flow and the shared steps are implementation, not just a contract.

---

## 3. When you need a constructor for initialization

An interface cannot have a constructor. If every subclass must run the same setup before it is usable, put that setup in an abstract-class constructor.

- **System design concept:** resource setup and a required initialization sequence.
- **Example:** a base class for AWS S3 integrations.

```java
public abstract class AbstractAwsStorageService {
    protected AmazonS3 s3Client;

    public AbstractAwsStorageService(String region) {
        this.s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .build();
    }

    public abstract void uploadFile(File file);
}
```

A subclass cannot skip the region setup. The constructor forces it.

---

## 4. When you need non-public access

Every method on an interface is public (or private to the interface itself, since Java 9, and those private methods cannot be inherited). If a helper must be visible to subclasses and hidden from callers, use `protected` or `private` on an abstract class.

- **System design concept:** encapsulation of an internal API.
- **Example:** a payment gateway whose logging and charge step should not be part of the public API the controller sees.

```java
public abstract class AbstractPaymentGateway {
    private void logTransaction(String txnId) {
        // hidden from everyone outside this class
    }

    // Visible to StripeGateway and other subclasses only.
    protected abstract boolean executeCharge(double amount);
}
```

---

## 5. When you are evolving an API

Adding a method to an interface forces every implementor to handle it. A `default` method avoids that only when the new behavior does not need the class's own fields. Adding a concrete method to an abstract class does not break existing subclasses. They inherit it.

- **System design concept:** backward compatibility in a framework or a shared base service.
- **Example:** 50 services extend `AbstractNotificationService`. A new `rateLimitCheck()` written once in the abstract class is available to all 50 with no change in the subclasses.

This is the practical reason abstract classes age well when the family of types shares state.

---

## When an abstract class is the best choice

Pick an abstract class when most of the following are true:

- The relationship is **IS-A**, and there is only one such parent. A class can extend one class.
- Subclasses share **fields**, not only method names.
- Some behavior is already known and should not be copied into every subclass.
- Some behavior is unknown and must be supplied by the subclass (`abstract` methods).
- You need a **constructor** so an object is never created half-initialized.
- You need `protected` or `private` helpers.
- You want to **ban** `new` on the base type. `abstract` makes `new BaseEntity()` a compile error, while still allowing `new Route()`.
- You want one method marked `final` so subclasses can customize steps but not the policy around those steps.

Skip the abstract class when the types are unrelated and only share a capability (`Flyable`, `Serializable`, `Comparable`), or when one class must take on several of those capabilities. That is an interface. In Spring, a pure service contract is also usually an interface: it is easy to mock, and a class can implement more than one.

A useful split in the same design: an interface for the contract callers depend on, and an abstract class that implements that interface and holds the shared code. Callers code to the interface. Subclasses extend the abstract class.

```java
public interface PaymentGateway {
    boolean charge(double amount);
}

public abstract class AbstractPaymentGateway implements PaymentGateway {
    private void logTransaction(String txnId) { }

    public final boolean charge(double amount) {
        logTransaction("txn");
        return executeCharge(amount);
    }

    protected abstract boolean executeCharge(double amount);
}
```

---

## Design patterns that fit an abstract class

These patterns want a partial implementation. An interface can name the operations. The abstract class is where the shared algorithm, the wrapped object, or the required hook lives.

| Pattern | Why the abstract class is the fit |
| --- | --- |
| Template Method | The base owns the final step order. Subclasses implement the open steps. |
| Factory Method | The base runs shared setup, then calls an abstract `create()` that each subclass implements. |
| Decorator | An abstract decorator holds the wrapped object and forwards the default behavior. Concrete decorators override one method. |
| State | An abstract state keeps the context reference and default no-op transitions. Each concrete state overrides the transitions it cares about. |
| Command | An abstract command stores the receiver and shared bookkeeping. `execute()` stays abstract. |
| Builder | An abstract builder holds the fields being assembled and a `build()` that checks them. Subclasses add the product-specific steps. |
| Composite | An abstract component can hold shared child handling. Use this when leaves and composites share real code, not only a type name. |

Strategy, Observer, and a pure Factory are usually interfaces. Move them to an abstract class only when the implementations start sharing fields or a fixed sequence of steps. At that point the abstract class is a Template Method sitting behind the strategy interface.

### Template Method

Already shown in `AbstractRouteProcessor`. The invariant is the order of steps.

### Factory Method

```java
public abstract class RouteProcessorFactory {
    public final RouteProcessor create(String region) {
        RouteProcessor processor = newProcessor();
        processor.configure(region);
        return processor;
    }

    protected abstract RouteProcessor newProcessor();
}
```

Every factory configures the region the same way. Only the product type changes.

### Decorator

```java
public abstract class RouteProcessorDecorator implements RouteProcessor {
    private final RouteProcessor delegate;

    protected RouteProcessorDecorator(RouteProcessor delegate) {
        this.delegate = delegate;
    }

    public Route process(Request req) {
        return delegate.process(req);
    }
}
```

Logging, caching, or timing each extend this class and wrap one extra step around `delegate`.

---

## Interview checklist

Ask these before choosing an abstract class:

- Is it an IS-A relationship? Example: `StripePayment` is a `PaymentGateway`.
- Do the subclasses share code and fields?
- Do you need to enforce an algorithm flow (Template Method)?
- Do you need to hide helpers (`protected` / `private`)?
- Do you need a constructor so setup cannot be skipped?
- Will you add shared behavior later without editing every subclass?

If the answers are yes, an abstract class is the better choice.

Use an interface instead when:

1. Unrelated classes share a contract. `Bird` and `Airplane` both implement `Flyable`.
2. You need more than one type parent. A class implements many interfaces and extends one class.
3. You are defining a pure capability: `Serializable`, `Comparable`, `Runnable`.

---

## Apply this in RouteForge

- `BaseRouteForgeException extends RuntimeException` holds the standard error code and timestamp. Concrete exceptions only set the code and the message.
- `AbstractRoutingAlgorithm` loads the graph once and leaves `findShortestPath()` abstract. Dijkstra and Bellman-Ford override that method and reuse the loading, validation, and result mapping.

That keeps the Spring services free of copied setup. Each new algorithm or exception adds the part that is actually different.

The same idea is already in this package: `CoffeeVendorSupplier` is the shared machine workflow, and `CoffeMachine` is one concrete machine. Callers use the public behavior. Heating, grinding, and brewing stay inside the type hierarchy.

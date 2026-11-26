# When to Use an Interface

In low-level design and system design, an interface is how you decouple a caller from a concrete class.

An abstract class says what a thing **is**. An interface says what a thing **can do**, and how the rest of the system is allowed to talk to it.

---

## Part 1: When to create an interface

### 1. A contract for unrelated classes (CAN-DO)

Use an interface when two classes that are not in the same family still need the same behavior.

In RouteForge, a `Vehicle` and a `Warehouse` are different entities. Both can be placed on a map.

```java
public interface Trackable {
    Coordinates getCoordinates();
}

public class Vehicle implements Trackable {
    public Coordinates getCoordinates() { /* vehicle location */ return null; }
}

public class Warehouse implements Trackable {
    public Coordinates getCoordinates() { /* warehouse location */ return null; }
}
```

The map code depends on `Trackable`. It does not care whether the object is a vehicle or a warehouse.

### 2. Architecture and decoupling (dependency inversion)

In Spring Boot, a controller should depend on a service interface, not on a concrete service class. You can swap the implementation, and a unit test can pass a mock.

`RouteController` depends on `RouteService`, not on `RouteServiceImpl`.

```java
public interface RouteService {
    Route plan(Request request);
}

@RestController
public class RouteController {
    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }
}
```

Spring injects whichever class implements `RouteService`. The controller stays the same.

### 3. Multiple inheritance of type

A class can extend only one class. It can implement many interfaces. Use interfaces when one type needs several capabilities.

A `DeliveryDrone` extends `Vehicle` and implements `Flyable`, `Trackable`, and `PayloadCarrier`.

```java
public class DeliveryDrone extends Vehicle implements Flyable, Trackable, PayloadCarrier {
}
```

`Vehicle` is the identity. The interfaces are the capabilities.

### 4. Plugin or Strategy

Use an interface when the core flow must stay fixed and the algorithm, or a third-party plugin, must be replaceable.

`PaymentGateway` is the contract. `StripePayment` and `PaypalPayment` are the plugins. Checkout code only sees `PaymentGateway`.

```java
public interface PaymentGateway {
    boolean charge(double amount);
}
```

The same shape is a Strategy: one interface, several interchangeable algorithms.

---

## Part 2: Interface or abstract class

Ask these four questions while designing.

| Diagnostic question | If yes | If no |
| --- | --- | --- |
| Is this core identity, or a peripheral capability? | "A car **is a** vehicle" → abstract class | "A car **can** refuel" → interface |
| Do the subclasses share fields? | They share `id`, `createdAt` → abstract class | They share only method signatures → interface |
| Do unrelated classes need this behavior? | Only a close family needs it → abstract class | `Bird` and `Airplane` both fly → interface |
| Do you need to hide helper methods? | You need `private` or `protected` helpers on the type hierarchy → abstract class | The surface is a public contract → interface |

Start with interfaces. They draw the boundary of the system. Add an abstract class only when several implementations are copying the same fields and the same code.

A class can do both: implement the interface the caller depends on, and extend an abstract class that holds the shared code.

---

## Part 3: Keywords and access rules

An interface exists to publish a contract. The keywords follow from that.

### Access

**On the interface itself**

- `public`: visible to the whole application.
- No modifier: visible only inside the same package.
- `private` and `protected` are not allowed on the interface declaration.

**On methods**

- A method with no body is implicitly `public` and `abstract`.
- `protected` is not allowed on an interface method. The contract is public to implementors.
- From Java 9, a `private` method is allowed only as a helper for other methods inside the same interface. Implementing classes do not inherit it.

**On fields**

- Every field is implicitly `public static final`. It is a constant.
- An interface cannot hold instance fields. Shared mutable state belongs on an abstract class.

### Other keywords

| Keyword | Meaning |
| --- | --- |
| `abstract` | A method with no body is already abstract. Writing the keyword is optional. |
| `static` | Java 8+. The method belongs to the interface, not to the implementing class. Implicitly `public`. |
| `default` | Java 8+. A method with a body. Implicitly `public`. Existing implementors inherit it, so you can extend the contract without editing every class. |
| `final` | Every interface field is already `final`. |

```java
public interface RouteOptimizer {

    // public abstract
    void optimizeRoute(Route route);

    // public static final
    int MAX_STOPS = 50;

    // default method: body is allowed, implementors inherit it
    default void logOptimization(Route route) {
        validateRoute(route);
        System.out.println("Optimizing...");
    }

    // static method: called on the interface, not on an instance
    static RouteOptimizer getDefault() {
        return new DijkstraOptimizer();
    }

    // private helper: visible only to methods in this interface
    private void validateRoute(Route route) {
        if (route == null) {
            throw new IllegalArgumentException();
        }
    }
}
```

A `default` method still cannot read instance fields of the implementing class, because the interface has none. If the new behavior needs `id` or `createdAt`, put it on an abstract class.

---

## Part 4: Other interface forms

### Marker interface

A marker interface has no methods and no constants. It tags a class so a framework or the JVM can treat it differently.

- `java.io.Serializable`
- `java.lang.Cloneable`

In RouteForge, an `Auditable` marker can tell a component to log changes for the classes that implement it. `@Entity` is a different tool: it is an annotation that tags a class for JPA, not an interface.

```java
public interface Auditable {
}

public class Route implements Auditable {
}
```

### Functional interface

A functional interface has exactly one abstract method. A lambda or a method reference can stand in for it. `@FunctionalInterface` is optional and makes the compiler reject a second abstract method.

`Runnable`, `Callable`, and `Comparator` are functional interfaces.

```java
@FunctionalInterface
public interface RouteCalculator {
    double calculate(Route route);
}

RouteCalculator toll = route -> route.getDistance() * 1.5;
```

`default` and `static` methods do not count toward that one abstract method.

### Sealed interface

Java 17 lets you list the only types allowed to implement the interface. The set of states is closed, which is what you want when a domain has a fixed list of outcomes.

```java
public sealed interface RouteStatus
        permits Planned, InProgress, Completed, Cancelled {
}

public record Planned(String routeId) implements RouteStatus {}
public record InProgress(String routeId) implements RouteStatus {}
public record Completed(String routeId) implements RouteStatus {}
public record Cancelled(String routeId, String reason) implements RouteStatus {}
```

A new status cannot appear unless it is added to `permits`. A `switch` on `RouteStatus` can then be exhaustive.

---

## Part 5: How this splits in RouteForge

**Interfaces, the contracts**

| Interface | Implementations | Why an interface |
| --- | --- | --- |
| `RoutingStrategy` | `DijkstraStrategy`, `AStarStrategy`, `BellmanFordStrategy` | Swap the algorithm at runtime. The caller does not change. |
| `CacheProvider` | `RedisCacheProvider`, `InMemoryCacheProvider` | Business code does not mention Redis or an in-memory map. |
| `NotificationSender` | `EmailSender`, `SmsSender`, `PushNotificationSender` | One send call, three channels. |

**Abstract classes, the shared implementation**

| Abstract class | What it owns |
| --- | --- |
| `BaseEntity` | `@Id`, `createdAt`, `updatedAt`. Shared state. |
| `AbstractRoutingStrategy` | Graph loading shared by Dijkstra and A*. Only the priority-queue step stays abstract. |
| `BaseRouteForgeException` | Error code and HTTP status used by every custom exception. |

`RoutingStrategy` is the interface the application depends on. `AbstractRoutingStrategy` is optional, and only worth adding when Dijkstra and A* are copying the same loading code. The interface stays the boundary. The abstract class is the shared body behind that boundary.

Contracts stay on interfaces. Shared fields and shared steps stay on abstract classes. Controllers and services then depend on the interface, which is what makes the Spring code replaceable in tests and in production.

---

## Part 6: Why the Factory pattern starts with an interface

**Question.** While learning the Factory pattern, step 1 is "define an interface", for example `NotificationService`. The Factory moves object creation out of the client and into a factory class. Why does that step need an interface, and which of the earlier interface points does it match?

### Which points it matches

Defining `NotificationService` matches two of the cases above.

**Dependency inversion (Part 1, point 2).** The Factory exists to decouple. The client must not know which notification class was created. It depends on the interface. The factory is the only place that sees `EmailNotification` or `SmsNotification`.

**A CAN-DO contract for multiple implementations (Part 1, point 1, and the plugin case in point 4).** `EmailNotification`, `SmsNotification`, and `PushNotification` are different classes. They share one capability: `sendNotification()`. The interface is that shared contract, so the factory can return any of them through one type. That is also a Strategy: one contract, several interchangeable implementations.

Without the interface, the factory is only a more complicated `new`. The pattern works because of polymorphism: the client calls the interface method, and the real object decides what that call does.

### Without the interface

If the factory returns a concrete class, the client is tied to that class.

```java
public class NotificationFactory {
    public EmailNotification createNotification(String type) {
        if ("EMAIL".equals(type)) {
            return new EmailNotification();
        }
        return null;
    }
}

NotificationFactory factory = new NotificationFactory();
EmailNotification myService = factory.createNotification("EMAIL");
myService.sendNotification("Route is ready");
```

The client must know `EmailNotification`. If the factory later returns `SmsNotification`, this client no longer compiles. Creation moved, and the coupling stayed.

### With the interface

```java
public interface NotificationService {
    void sendNotification(String message);
}

public class EmailNotification implements NotificationService {
    public void sendNotification(String message) {
        // send email
    }
}

public class SmsNotification implements NotificationService {
    public void sendNotification(String message) {
        // send sms
    }
}

public class NotificationFactory {
    public NotificationService createNotification(String type) {
        if ("EMAIL".equals(type)) {
            return new EmailNotification();
        }
        if ("SMS".equals(type)) {
            return new SmsNotification();
        }
        throw new IllegalArgumentException("Unknown notification type: " + type);
    }
}

NotificationFactory factory = new NotificationFactory();
NotificationService service = factory.createNotification("EMAIL");
service.sendNotification("Route is ready");
```

The client sees `NotificationService` only. Adding `WhatsAppNotification` means a new class and one new branch in the factory. The client stays as it is.

### The idea to keep

The interface is the common return type, the common denominator.

A factory is like a vending machine.

- `NotificationService` is the generic beverage slot.
- `EmailNotification` and `SmsNotification` are the specific cans inside.
- The factory is the mechanism that drops one can into that slot.

The person using the machine only needs to know the slot. They do not need a different slot shape for every drink. The interface standardizes what comes out, so the client has one way to call `sendNotification()` no matter which class the factory built.

/**
 * HelpRequest - interface defining the contract for all request types.
 * Demonstrates: Abstraction and Polymorphism.
 */
public interface HelpRequest {
    String getId();
    String getName();
    String getCategory();
    String getMessage();
    String getTimestamp();
    String toJson();
}

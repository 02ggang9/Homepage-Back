package keeper.project.homepage.election.exception;

public class CustomElectionIsNotClosedException extends RuntimeException {

  public CustomElectionIsNotClosedException(String msg, Throwable t) {
    super(msg, t);
  }

  public CustomElectionIsNotClosedException(String msg) {
    super(msg);
  }

  public CustomElectionIsNotClosedException() {
    super();
  }

}

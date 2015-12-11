package logic;

public class Field {
  
 
  public Field(int id, FieldStatus status) {
    super();
    setId(id);
    setStatus(status);
  }

  private int id;
  private FieldStatus status;
  
  public int getId() {
    return id;
  }
  private void setId(int id) {
    this.id = id;
  }
  public FieldStatus getStatus() {
    return status;
  }
  public void setStatus(FieldStatus status) {
    this.status = status;
  }
  
  @Override
  public String toString() {
    return "Field [id=" + this.id + ", status=" + this.status + "]";
  }
  
}

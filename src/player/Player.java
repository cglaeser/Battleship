package player;

import java.util.List;

import logic.Field;

public interface Player{
  
  public List<Field> getFields();
  
  public Field getField(int field);
  
  public void setFieldStatus(Field f);
  
}

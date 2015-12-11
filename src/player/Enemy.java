package player;

import java.util.ArrayList;
import java.util.List;

import logic.Field;
import logic.FieldStatus;

public class Enemy implements Player{
  
  private List<Field> fields;
  
  public Enemy(int firstPos, int lastPos){
    fields = new ArrayList<Field>(lastPos-firstPos+1);
    
    for(int i=firstPos;i <= lastPos;i++){
      fields.add(new Field(i,FieldStatus.UNKNOWN));
    }
  }

  @Override
  public List<Field> getFields() {
    return fields;
  }

  @Override
  public Field getField(int field) {
    return fields.get(field);
  }

  @Override
  public void setFieldStatus(Field f) {
    // TODO Auto-generated method stub
    
  }
  
}

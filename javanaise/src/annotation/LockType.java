package annotation;

public enum LockType {
   read("read"), write("write");
   
   private String type;
   
   LockType(String type){
      this.type = type;
   }
   
   public String toString(){
      return type;
   }
}
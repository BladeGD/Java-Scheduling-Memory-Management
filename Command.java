
public class command {
    protected String type;
    protected int variableID;
    protected int value;

    public static void main(String args[]) {

    }

    public command(String t, int id, int val){
        type = t;
        variableID = id;
        value = val;
    }

    public command(command c){
        this(c.type, c.variableID, c.value);
    }

    public void setType(String t){
        type = t;
    }

    public void setID(int id){
        variableID = id;
    }

    public void setValue(int val){
        value = val;
    }

    public String getType(){
        return this.type;
    }

    public int getID(){
        return this.variableID;
    }

    public int getValue(){
        return this.value;
    }

}
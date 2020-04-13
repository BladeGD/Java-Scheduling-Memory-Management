public class mem {
    protected int var_ID;
    protected int var_val;
    protected int var_age;

    public static void main(String args[]) {

    }

    public mem(int id, int val, int a) {
        var_ID = id;
        var_val = val;
        var_age = a;
    }

    public mem(mem m) {
        this(m.var_ID, m.var_val, m.var_age);
    }

    public void setID(int id){
        var_ID = id;
    }

    public void setVal(int val){
        var_val = val;
    }

    public void setAge(int a){
        var_age = a;
    }

    public int getID(){
        return this.var_ID;
    }

    public int getVal(){
        return this.var_val;
    }

    public int getAge(){
        return this.var_age;
    }

}
//Micaela Landauro
//CS 4348.002

import java.io.*;
import java.util.ArrayList;

class BuddyObject{
    private int size = 0;
    private char name = ' ';
    private boolean isAllocated = false;

    //initialize with only size
    public BuddyObject(int size){
        this.size = size;
    }

    public void setName(int name){
        this.name = (char) name;
    }
    public char getName(){return name;}

    public boolean getAllocation(){return isAllocated;}
    public void setAllocation(boolean allocate){isAllocated = allocate;}

    public int getSize(){return size;}
    public void setSize(int size){this.size = size;}

}

public class BuddySystem {
    private ArrayList<ArrayList<BuddyObject>> system = new ArrayList<>();
    public BuddySystem(){
        //initialize array list
        for(int i = 0; i < 5; i++)
            system.add(new ArrayList<>());
        //start with 1024 of memory
        system.get(0).add(new BuddyObject(1024));
    }

    public boolean request(int amount, int name){
        BuddyObject temp;
        //get layer to check
        int layer = getLayer(amount);

        //check layer given for open memory
        if(!system.get(layer).isEmpty()){//given layer has memory available
            //check that there is open memory
            for(int j = 0; j < system.get(layer).size(); j++){
                temp = system.get(layer).get(j);
                //set as allocated and give name
                if(!temp.getAllocation()){
                    temp.setName(name);
                    temp.setAllocation(true);
                    temp.setSize(amount);
                    return true;
                }
            }
        }

        //search for next layer with memory avail
        int layerCopy = layer;
        boolean found = false;
        while(!found){
            if(!found && layerCopy < 0) { //no memory left to allocate
                System.out.println("ERROR: Couldn't allocate memory");
                return false;
            }
            for(int f = 0; f < system.get(layerCopy).size(); f++){
                if(!system.get(layerCopy).get(f).getAllocation()) {
                    found = true;//found empty for allocation
                    break;
                }
            }
            if(!found)
                layerCopy--;//go up to next level
        }

        //split memory until desired layer
        while(layer != layerCopy){
            temp = system.get(layerCopy).get(0);
            //remove memory to split from previous layer
            for(int k = 0; k < system.get(layerCopy).size(); k++){
                if(!system.get(layerCopy).get(k).getAllocation()) {//if not allocated, remove
                    system.get(layerCopy).remove(k);
                    break;
                }
            }

            layerCopy++;//move to next layer down
            //make two copies
            system.get(layerCopy).add(new BuddyObject(temp.getSize()/2));
            system.get(layerCopy).add(new BuddyObject(temp.getSize()/2));
        }

        //now can allocate to first available memory slot
        for(int j = 0; j < system.get(layer).size(); j++){
            temp = system.get(layer).get(j);
            if(!temp.getAllocation()){
                temp.setName(name);
                temp.setAllocation(true);
                temp.setSize(amount);
                return true;
            }
        }
        return false;//couldn't allocate
    }

    public int getLayer(int amt){
        int layer = (int) Math.ceil(Math.log(amt)/Math.log(2));

        switch(layer){
            case 10:
                return 0;
            case 9:
                return 1;
            case 8:
                return 2;
            case 7:
                return 3;
            case 6:
                return 4;
            default:
                return -1;
        }
    }

    public int returnLayer(int layer){
        switch(layer){
            case 0:
                return 10;
            case 1:
                return 9;
            case 2:
                return 8;
            case 3:
                return 7;
            case 4:
                return 6;
            default:
                return -1;
        }
    }

    public boolean release(char name){
        int layer = -1;
        int location = -1;
        //find memory and release
        for(int i = 0; i < system.size(); i++){
            for(int j = 0; j < system.get(i).size(); j++){
                BuddyObject temp = system.get(i).get(j);
                if(temp.getName() == name){
                    //system.get(i).remove(j);//release found memory
                    system.get(i).get(j).setName(' ');
                    system.get(i).get(j).setAllocation(false);
                    layer = i;
                    location = j;
                }
            }
        }

        //check to make sure memory name exists
        if(layer == -1 || location == -1) {
            System.out.println("ERROR: memory name does not exist");
            return false;
        }

        //go through whole system and merge appropriate buddies
        boolean buddied = true;
        while(buddied){
            //go through whole system and merge appropriate buddies
            for(int x = 0; x < system.size(); x++) {//x == x
                for (int y = 0; y < system.get(x).size(); y++) { //y == y
                    if (system.get(x).size() == 1) {//if only one element, can't buddy
                        buddied = false;
                        continue;
                    } else {
                        if(system.get(x).get(y).getAllocation())
                            continue;
                        if (y < system.get(x).size() - 1 && system.get(x).get(y + 1).getAllocation() == false) {
                            //merge
                            system.get(x - 1).add(new BuddyObject((int) Math.pow(2, returnLayer(x)) * 2));
                            system.get(x).remove(y + 1);
                            system.get(x).remove(y);
                            buddied = true;
                        } else if (y > 0 && system.get(x).get(y - 1).getAllocation() == false) {
                            //merge
                            system.get(x - 1).add(new BuddyObject((int) Math.pow(2, returnLayer(x)) * 2));
                            system.get(x).remove(y);
                            system.get(x).remove(y - 1);
                            buddied = true;
                        } else {
                            //nothing to merge
                            buddied = false;
                            continue;
                        }
                    }
                }
            }
        }
        return true;
    }

    public void displayBlocks(){
        String middle = "";

        for(int i = 0; i < system.size(); i++) {
            if(system.get(i).size() > 0){//there is memory there so print it
                for (int j = 0; j < system.get(i).size(); j++) {
                    //top += "--------------";
                    int size = (int) Math.pow(2, returnLayer(i));
                    String sizeFormat = String.format("%4s", Integer.toString(size));
                    if(system.get(i).get(j).getAllocation()) {
                        middle += "| " + system.get(i).get(j).getName() + "    " + sizeFormat + "K ";
                    } else
                        middle += "|      " + sizeFormat + "K ";
                    //bottom += "--------------";
                }
            }
        }
        middle += "|";
        String lines = String.format("%"+middle.length()+"s", "").replace(' ', '-');
        System.out.println(lines);
        System.out.println(middle);
        System.out.println(lines);
        System.out.println();
    }
    public static void main(String [] args) throws Exception{
    /*
    Request 100K
    Request 240K
    Request 64K
    Release C
    Release A
    Release B
         */
        BuddySystem sys = new BuddySystem();
        sys.displayBlocks();
        int name = 65;

        String filename = args[0]; //gets file name from terminal
        File f = new File(filename);

        //TESTING
//        File f = new File("src/sample3.txt");

        try{
            FileInputStream fstream = new FileInputStream(f);
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

            String token;
            while((token = br.readLine()) != null){ //keep reading until end of file
                String [] input = token.split(" ");
                String cmd = input[0];
                String var = input[1];
                int size = 0;
                if(Character.isDigit(var.charAt(0))){
                    var = var.substring(0, var.length()-1);
                    size = Integer.parseInt(var);
                }
                System.out.println(token);
                if(cmd.equals("Request") || cmd.equals("request")){
                    if(sys.request(size, name) == false)
                        break;
                    name++;
                    sys.displayBlocks();
                } else if(cmd.equals("Release") || cmd.equals("release")){
                    if(sys.release(var.charAt(0)) == false)
                        break;
                    sys.displayBlocks();
                }
                else{
                    System.out.println("Request could not be satisfied");
                    break;
                }
            }

            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

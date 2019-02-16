//package com.company;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import org.graphstream.graph.*;
import org.graphstream.graph.implementations.*;
import org.graphstream.graph.Edge;
import java.io.Serializable;
import java.util.Random;


class DataHolder implements Serializable{
    public HashMap<String, Integer> assignmentMap;
    public HashMap<String, HashSet<Integer>> domains;
    DataHolder(){
        assignmentMap = new HashMap<String, Integer>();
        domains = new HashMap<String, HashSet<Integer>>();
    }
}

public class Main {
    static Graph graph;
    static int edgeCounter=0, variableCounter=0;
    static ArrayList<String> variableList = new ArrayList<String>();
    static DataHolder finalData;
    static Set<String> fixedList = new HashSet<String>();
    static HashMap<String, Integer> unaryList = new HashMap<String,Integer>();
    static ArrayList<String> conflictList = new ArrayList<>();
    static ArrayList<String > correctList = new ArrayList<>();
    static int backTrack=0;
    static boolean mrv_enabled=false;
    static boolean isUnary(String x){
        try{
            Integer.parseInt(x);
            return true;
        }catch(NumberFormatException e){
            return false;
        }
    }
    static DataHolder fileParserforMinConflicts(String filePath) throws IOException{
        /* Parse input file and store data in DataHolder object*/
        DataHolder d = new DataHolder();

        BufferedReader br = new BufferedReader(new FileReader(filePath));
        String st;
        boolean vars=true, constraints=false;
        while((st=br.readLine())!=null){
            //System.out.println(st);
            if(st.equalsIgnoreCase("ENDVARS")||st.equalsIgnoreCase("VARS")
                    ||st.equalsIgnoreCase("ENDCONSTRAINTS")){
                constraints=false;
                continue;
            }
            if(st.equalsIgnoreCase("CONSTRAINTS")){
                //System.out.println("constraints royy");
                constraints=true;
                vars=false;
                continue;
            }
            if(vars){
                variableCounter++;
                HashSet<Integer> hs = new HashSet<Integer>();
                int i=0;
                StringTokenizer stk = new StringTokenizer(st,":");
                String var=stk.nextToken();
                String optionsInString=stk.nextToken();
                //System.out.println(optionsInString);
                var = var.replaceAll(" ","");
                //System.out.println(var);
                graph.addNode(var);
                stk=new StringTokenizer(optionsInString," ");
                while(stk.hasMoreTokens()){
                    hs.add(Integer.parseInt(stk.nextToken()));
                    //System.out.println("yo");
                }
                d.domains.put(var,hs);
                //int randomAssignment = (variableCounter%hs.size()==0)?hs.size():variableCounter%hs.size();
                //d.assignmentMap.put(var,randomAssignment);
                variableList.add(var);
                continue;
            }
            if(constraints){
                //System.out.println("current constraint: "+st);
                StringTokenizer stk = new StringTokenizer(st," ");
                String comparator = stk.nextToken();
                //System.out.println("comparator: "+comparator);
                HashSet<String> hs= new HashSet<String>();
                while(stk.hasMoreTokens()) {
                    String x = stk.nextToken();
                    if (!isUnary(x)) {
                        //System.out.println("yo wassup string: " + x);
                        for (String t : hs) {
                            //System.out.println("yo wassup maaan: " + t);
                            try {
                                Edge edge = graph.addEdge(String.valueOf(edgeCounter++), t, x);
                                edge.addAttribute("operation", comparator);
                            } catch (EdgeRejectedException err) {
                            }
                        }
                        hs.add(x);
                    }else{
                        stk = new StringTokenizer(st," ");
                        comparator=stk.nextToken();
                        if(comparator.equals("=")){
                            String curr=stk.nextToken();
                            int v = Integer.parseInt(stk.nextToken());
                            d.assignmentMap.put(curr,v);
                            unaryList.put(curr,v);
                            HashSet<Integer> hst = d.domains.get(curr);
                            hst.clear();
                            hst.add(v);
                        }
                        //to add more support for other unary operations
                    }
                }
            }
        }
        return d;
    }
    static DataHolder fileParserforBackTracking(String filePath) throws IOException {
        /*File parsing of input files and storing information in DataHolder object*/
        DataHolder d = new DataHolder();
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        String st;
        boolean vars=true, constraints=false;
        while((st=br.readLine())!=null){
            //System.out.println(st);
            if(st.equalsIgnoreCase("ENDVARS")||st.equalsIgnoreCase("VARS")
            ||st.equalsIgnoreCase("ENDCONSTRAINTS")){
                constraints=false;
                continue;
            }
            if(st.equalsIgnoreCase("CONSTRAINTS")){
                //System.out.println("constraints royy");
                constraints=true;
                vars=false;
                continue;
            }
            if(vars){
                variableCounter++;
                HashSet<Integer> hs = new HashSet<Integer>();
                int i=0;
                StringTokenizer stk = new StringTokenizer(st,":");
                String var=stk.nextToken();
                String optionsInString=stk.nextToken();
                //System.out.println(optionsInString);
                var = var.replaceAll(" ","");
                //System.out.println(var);
                graph.addNode(var);
                stk=new StringTokenizer(optionsInString," ");
                while(stk.hasMoreTokens()){
                    hs.add(Integer.parseInt(stk.nextToken()));
                    //System.out.println("yo");
                }
                d.domains.put(var,hs);
                variableList.add(var);
                continue;
            }
            if(constraints){
                //System.out.println("current constraint: "+st);
                StringTokenizer stk = new StringTokenizer(st," ");
                String comparator = stk.nextToken();
                //System.out.println("comparator: "+comparator);
                HashSet<String> hs= new HashSet<String>();
                while(stk.hasMoreTokens()) {
                    String x = stk.nextToken();
                    if (!isUnary(x)) {
                        //System.out.println("yo wassup string: " + x);
                        for (String t : hs) {
                            //System.out.println("yo wassup maaan: " + t);
                            try {
                                Edge edge = graph.addEdge(String.valueOf(edgeCounter++), t, x);
                                edge.addAttribute("operation", comparator);
                            } catch (EdgeRejectedException err) {
                            }
                        }
                        hs.add(x);
                    }else{
                        stk = new StringTokenizer(st," ");
                        comparator=stk.nextToken();
                        if(comparator.equals("=")){
                            String curr=stk.nextToken();
                            int v = Integer.parseInt(stk.nextToken());
                            d.assignmentMap.put(curr,v);
                            HashSet<Integer> hst = d.domains.get(curr);
                            hst.clear();
                            hst.add(v);
                        }
                        //to add more support for other unary operations
                    }
                }
            }
        }
        return d;
    }

    static String pickFromRemaining(DataHolder d){
        //Used to pick unassigned variables
        String ret=null;
        int mrv = Integer.MAX_VALUE;
        Set<String> vars=d.domains.keySet();
        Set<String> assigned_keyset = d.assignmentMap.keySet();
        for(String x: vars){
            if(!assigned_keyset.contains(x)){
                if(mrv_enabled) {
                    int current_domain_size = d.domains.get(x).size();
                    if (mrv > current_domain_size) {
                        mrv = current_domain_size;
                        ret = x;
                    }
                }else return x;
            }
        }
        return ret;
    }
    static boolean consistencyCheck(HashMap<String, Integer> ass, String var, int val){
        //To check if var=val assignment leads to any inconsistency with constraints.
        //System.out.println("consistencyCheck: Variable: "+var);
        Node curr = graph.getNode(var);
        Iterator<Node> k = curr.getNeighborNodeIterator();
        //System.out.println(k.)
        while(k.hasNext()){
            Node next = k.next();
            //if(var=="Milk")
            //System.out.println("Neighbor Node: "+next.getId());
            if(next.getId().equals(var)) continue;
            //System.out.println("Current Assignment set: "+ass);
            //System.out.println("Neighbor Node assignment: "+ass.get(next.getId()));
            //if(ass.get(next.getId())!=null&&ass.get(next.getId())==val) return false;
            Edge e = curr.getEdgeBetween(next);
            String operation = e.getAttribute("operation");
            if(ass.get(next.getId())!=null&&!constraintSatisfied(operation,ass.get(next.getId()),val)) return false;
        }
        return true;
    }
    static DataHolder getClone(DataHolder d) throws IOException, ClassNotFoundException {
        //Used for creating clone objects
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(d);
        oos.flush();
        oos.close();
        bos.close();
        byte[] byteData = bos.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(byteData);
        ObjectInputStream ois = new ObjectInputStream(bais);
        DataHolder d1 = (DataHolder) ois.readObject();
        return d1;
    }
    static boolean constraintSatisfied(String operation, int curr, int next){
        //Used to check if constraint between two connected variables is satisfied.
        if(operation.equals("=")||operation.equalsIgnoreCase("equal")
        ||operation.equalsIgnoreCase("Allmatch")) return (curr==next)?true:false;
        if(operation.equals("!=")||operation.equalsIgnoreCase("Alldiff")
        ||operation.equalsIgnoreCase("Not equal")) return (curr!=next)?true:false;
        if(operation.equals("<")) return (curr<next)?true:false;
        if(operation.equals(">")) return (curr>next)?true:false;
        if(operation.equals("<1")) return (curr+1==next)?true:false;
        if(operation.equals(">1")) return (curr-1==next)?true:false;
        if(operation.equals("<>1")) {
            if(curr+1==next) return true;
            else if(curr-1==next) return true;
            return false;
        }
        System.out.println("No matching constraint found in our base. Returning false");
        return false;
    }
    static boolean BackTrack(DataHolder d) throws CloneNotSupportedException, IOException, ClassNotFoundException {
        String var = pickFromRemaining(d);
        finalData=d;
        if(var==null){
            //finalData=d;
            return true;
        }
        outerloop: for(int val: d.domains.get(var)){
            //System.out.println("for "+var+" domain: "+d.domains.get(var));
            boolean checker = consistencyCheck(d.assignmentMap,var,val);
            if(checker){
                //DataHolder d1 = (DataHolder) d.clone();
                DataHolder d1 = getClone(d);
                Node curr = graph.getNode(var);
                Iterator<Node> k = curr.getNeighborNodeIterator();
                while(k.hasNext()){
                    Node next = k.next();
                    HashSet<Integer> next_domain = d1.domains.get(next.getId());
                    Edge e = curr.getEdgeBetween(next);
                    String operation = e.getAttribute("operation");
                    //System.out.println("Neighbor node "+next.getId()+" current domain: "+next_domain);
                    //System.out.println(val+" must be removed from "+next.getId());
                    for(int x: next_domain){
                        if(!constraintSatisfied(operation,val,x)) {
                        //if(val==x){
                            next_domain.remove(x);
                            break;
                        }
                    }
                    //System.out.println(next_domain);
                    if(next_domain.size()==0) continue outerloop;
                }
                d1.assignmentMap.put(var,val);
                boolean checker2=BackTrack(d1);
                //System.out.println("checker2: "+checker2);
                if(checker2) return true;
                else backTrack++;
            }
            //System.out.println("for "+var+" domain: "+d.domains.get(var));
        }
        finalData=d;
        return false;
    }
    static boolean isSolution(DataHolder d){
        Set<String> assignedVars = d.assignmentMap.keySet();
        for(String curr_var: assignedVars){
            Node curr_node = graph.getNode(curr_var);
            Iterator<Node> itr = curr_node.getNeighborNodeIterator();
            while(itr.hasNext()){
                Node next = itr.next();
                Edge curr_edge = curr_node.getEdgeBetween(next);
                String constraint = curr_edge.getAttribute("operation");
                if(!constraintSatisfied(constraint,d.assignmentMap.get(curr_var),d.assignmentMap.get(next.getId())))
                    return false;
            }
        }
        return true;
    }
    static int counter=0;
    static int conflictCounter(HashMap<String, Integer> assignments, String var, int val){
        Node curr = graph.getNode(var);
        Iterator<Node> itr = curr.getNeighborNodeIterator();
        int conflictCount=0;
        while(itr.hasNext()){
            Node next = itr.next();
            Edge edge = curr.getEdgeBetween(next);
            String operation = edge.getAttribute("operation");
            if(!constraintSatisfied(operation,val,assignments.get(next.getId()))) conflictCount++;
        }
        return conflictCount;
    }
    static String pickForMinConflicts(DataHolder d){
        Random rand = new Random();
        return variableList.get(rand.nextInt(variableList.size()));
    }
    static DataHolder randomAssigner(DataHolder d){
        ////Random assignment of values to variables
        Set<String> vars = d.domains.keySet();
        for(String var: vars){
            HashSet<Integer> hs = d.domains.get(var);
            Random rand = new Random();
            int index = rand.nextInt(hs.size());
            Iterator<Integer> iter = hs.iterator();
            for (int i = 0; i < index; i++) {
                iter.next();
            }
            d.assignmentMap.put(var,iter.next());
        }
        return d;
    }
    static void MinConflicts(DataHolder d, int max_steps){
        //Random assignment of values to variables
        d = randomAssigner(d);
        //1. Iterate for max_steps, fixing(minimum conflicts) one variable at a time.
        for(int i=0;i<max_steps;i++){
            //2. If current assignment is solution, return.
            if(isSolution(d)){
                System.out.println("solution found by MinConflicts Algorithm in "+(i+1)+" steps");
                finalData=d;
                return;
            }
            //3. Pick a variable randomly
            String var=pickForMinConflicts(d);
            HashSet<Integer> options = d.domains.get(var);
            int minConflicts=Integer.MAX_VALUE;
            int bestOption=Integer.MAX_VALUE;
            ArrayList<Integer> al = new ArrayList<Integer>();
            for(int val: options){
                int curr_conflicts = conflictCounter(d.assignmentMap,var,val);
                if(minConflicts>curr_conflicts){
                    minConflicts=curr_conflicts;
                    bestOption=val;
                    al.clear();
                    al.add(val);
                }else if(minConflicts==curr_conflicts){
                    al.add(val);
                }
            }
            //Tie breaking between different values of equal number of conflicts
            Random rand = new Random();
            int randPick = al.get(rand.nextInt(al.size()));
            d.assignmentMap.put(var,randPick);
        }
        finalData=d;
    }
    static void solutionEvaluator(){
        //Takes all the final data from algorithms and checks for correctness.
        Iterator<String> itr = finalData.assignmentMap.keySet().iterator();
        while(itr.hasNext()){
            String curr=itr.next();
            if((unaryList.containsKey(curr)&&unaryList.get(curr)==finalData.assignmentMap.get(curr))
                    ||consistencyCheck(finalData.assignmentMap,curr, finalData.assignmentMap.get(curr)))
                correctList.add(curr+"="+finalData.assignmentMap.get(curr));
            else
                conflictList.add(curr+"="+finalData.assignmentMap.get(curr));
        }
    }
    static void createFile(String data, String algorithm) throws IOException {
        /*Create output file with path information*/
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("hh-mm-ss");
        String strDate = dateFormat.format(date);
        String fileName=algorithm+"_"+strDate+".txt";
        Path currentRelativePath = Paths.get("");
        String path = currentRelativePath.toAbsolutePath().toString();
        FileOutputStream out = new FileOutputStream(fileName);
        out.write(data.getBytes());
        out.close();
        System.out.println("For output file, please check path: " + path+" with file name: "+fileName);
    }
    public static void main(String[] args) throws IOException, CloneNotSupportedException, ClassNotFoundException {
        graph = new SingleGraph("project2");
        if(args[1].equalsIgnoreCase("backtrack")) {
            if(args.length>3&&args[3].equalsIgnoreCase("mrv")) mrv_enabled=true;
            DataHolder data = fileParserforBackTracking(args[0]);
            BackTrack(data);
            //System.out.println(data.domains);
            if(mrv_enabled) System.out.println("Minimum Remaining Values Heuristic is enabled");
            else System.out.println("Minimum Remaining Values Heuristic is not enabled");
            //System.out.println("final data: " + finalData.assignmentMap);
            System.out.println("Number of backtrack steps taken: "+backTrack);
            if(isSolution(finalData)){
                  System.out.println("Solution found");
            }else System.out.println("Solution not found");
            System.out.println("Final Assignments: "+finalData.assignmentMap);
            solutionEvaluator();
            System.out.println("Following "+correctList.size()+" are correct assignments"+"\n"+correctList);
            System.out.println("Following "+conflictList.size()+" are conflicting assignments"+"\n"+conflictList);
            Iterator<String> itr1 = correctList.iterator();
            String d = "BackTrack approach. Given Problem: "+args[0]+"\nCorrect Assignments below:\n";
            while(itr1.hasNext()){
                d = d + itr1.next()+"\n";
            }
            Iterator<String> itr2 = conflictList.iterator();
            d = d + "InCorrect Assignments below:\n";
            while(itr2.hasNext()){
                d = d + itr2.next()+"\n";
            }
            createFile(d,"BackTrack");
        }else{
            DataHolder data = fileParserforMinConflicts(args[0]);
            MinConflicts(data,Integer.parseInt(args[2]));
            if(isSolution(finalData)){
            }else System.out.println("Solution not found");
            System.out.println("Final Assignments: "+finalData.assignmentMap);
            solutionEvaluator();
            System.out.println("Please note that as min conflicts involves random initial assignments, solutions might vary on each run");
            System.out.println("Following "+correctList.size()+" are correct assignments"+"\n"+correctList);
            System.out.println("Following "+conflictList.size()+" are conflicting assignments"+"\n"+conflictList);
            Iterator<String> itr1 = correctList.iterator();
            String d = "MinConflicts approach. Given Problem: "+args[0]+"\nCorrect Assignments below:\n";
            while(itr1.hasNext()){
                d = d + itr1.next()+"\n";
            }
            Iterator<String> itr2 = conflictList.iterator();
            d =d+ "InCorrect Assignments below:\n";
            while(itr2.hasNext()){
                d = d + itr2.next()+"\n";
            }
            createFile(d,"MinConflicts");
        }
    }
}

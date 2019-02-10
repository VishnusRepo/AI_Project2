package com.company;

import java.io.*;
import java.util.*;

import org.graphstream.graph.*;
import org.graphstream.graph.implementations.*;
import org.graphstream.graph.Edge;
import java.io.Serializable;

import javax.xml.crypto.Data;


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
    static HashSet<String> variableList = new HashSet<String>();
    static DataHolder finalData;
    static boolean isUnary(String x){
        try{
            Integer.parseInt(x);
            return true;
        }catch(NumberFormatException e){
            return false;
        }
    }
    static DataHolder fileParser(String filePath) throws IOException {
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
                System.out.println("constraints royy");
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
                System.out.println("current constraint: "+st);
                StringTokenizer stk = new StringTokenizer(st," ");
                String comparator = stk.nextToken();
                System.out.println("comparator: "+comparator);
                HashSet<String> hs= new HashSet<String>();
                while(stk.hasMoreTokens()) {
                    String x = stk.nextToken();
                    if (!isUnary(x)) {
                        System.out.println("yo wassup string: " + x);
                        for (String t : hs) {
                            System.out.println("yo wassup maaan: " + t);
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
        String ret=null;
        int mrv = Integer.MAX_VALUE;
        Set<String> vars=d.domains.keySet();
        Set<String> assigned_keyset = d.assignmentMap.keySet();
        for(String x: vars){
            if(!assigned_keyset.contains(x)){
                int current_domain_size = d.domains.get(x).size();
                if(mrv>current_domain_size){
                    mrv=current_domain_size;
                    ret=x;
                }
            }
        }
        return ret;
    }
    static boolean consistencyCheck(HashMap<String, Integer> ass, String var, int val){
        System.out.println("consistencyCheck: Variable: "+var);
        Node curr = graph.getNode(var);
        Iterator<Node> k = curr.getNeighborNodeIterator();
        //System.out.println(k.)
        while(k.hasNext()){
            Node next = k.next();
            System.out.println("Neighbor Node: "+next.getId());
            if(next.getId().equals(var)) continue;
            System.out.println("Current Assignment set: "+ass);
            //System.out.println("Neighbor Node assignment: "+ass.get(next.getId()));
            //if(ass.get(next.getId())!=null&&ass.get(next.getId())==val) return false;
            Edge e = curr.getEdgeBetween(next);
            String operation = e.getAttribute("operation");
            if(ass.get(next.getId())!=null&&!constraintSatisfied(operation,ass.get(next.getId()),val)) return false;
        }
        return true;
    }
    static DataHolder getClone(DataHolder d) throws IOException, ClassNotFoundException {
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
        System.out.println("BackTrack: Current assignments: "+d.assignmentMap);
        System.out.println("Domains: "+d.domains);
        String var = pickFromRemaining(d);
        if(var==null){
            finalData=d;
            return true;
        }
        outerloop: for(int val: d.domains.get(var)){
            System.out.println("for "+var+" domain: "+d.domains.get(var));
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
                    System.out.println("Neighbor node "+next.getId()+" current domain: "+next_domain);
                    System.out.println(val+" must be removed from "+next.getId());
                    for(int x: next_domain){
                        if(!constraintSatisfied(operation,val,x)) {
                        //if(val==x){
                            next_domain.remove(x);
                            break;
                        }
                    }
                    System.out.println(next_domain);
                    if(next_domain.size()==0) continue outerloop;
                }
                d1.assignmentMap.put(var,val);
                boolean checker2=BackTrack(d1);
                System.out.println("checker2: "+checker2);
                if(checker2) return true;//else continue next
            }
            System.out.println("for "+var+" domain: "+d.domains.get(var));
        }
        return false;
    }

    public static void main(String[] args) throws IOException, CloneNotSupportedException, ClassNotFoundException {
        graph = new SingleGraph("project2");
        DataHolder data = fileParser(args[0]);
        //DataHolder d1 = (DataHolder) getClone(data);
        //d1.assignmentMap.put("a",1);
        //System.out.println(data.assignmentMap);
        //System.out.println(d1.assignmentMap);
        //System.out.println(d1.domains);
        BackTrack(data);
        System.out.println(data.domains);
        System.out.println("final data: "+finalData.assignmentMap);
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serversoma;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

/**
 *
 * @author renan
 */
public class WorkerDyn {
    
    String param;
    String linhaComando;
    String linhasArq;
    String nomeFunc;
    
    public String processaDyn(String caminho) throws FileNotFoundException{
        File f = new File(caminho);
        Scanner scan = new Scanner(f);
        String result = new String();
        while (scan.hasNextLine()){
            linhasArq = linhasArq.concat(scan.nextLine()); 
        }
        String[] splitter = linhasArq.split("<%");
        String[] splitter2 = splitter[1].split("%>");
        linhaComando = splitter2[0];
        
        linhaComando.trim();
        
        nomeFunc = linhaComando.substring(0, linhaComando.indexOf("("));
        //System.out.println(nomeFunc);
        linhaComando= linhaComando.replace("\"", "");
        
        param = linhaComando.substring(linhaComando.indexOf("(")+1, linhaComando.indexOf(")"));
        
        //System.out.println(param);
        result = calculaData(nomeFunc, param);
        //System.out.println("ANTES: "+linhasArq);
        //linhasArq =linhasArq.replace("\n", "");
        linhasArq =linhasArq.trim();
        linhasArq =linhasArq.replace("date(\"d-M-y\")", result);
        
//System.out.println("DEPOIS: " +linhasArq);
        
        
        return linhasArq;
    }
    
    public String calculaData(String nome, String param){
        SimpleDateFormat sdata = new SimpleDateFormat(param);
        Date data = new Date();
        //System.out.println(sdata.format(data).toString());
        return (sdata.format(data).toString());
    }

    public String getLinhaComando() {
        return linhaComando;
    }

    public void setLinhaComando(String linhaComando) {
        this.linhaComando = linhaComando;
    }

    public String getNomeFunc() {
        return nomeFunc;
    }

    public void setNomeFunc(String nomeFunc) {
        this.nomeFunc = nomeFunc;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public String getLinhasArq() {
        return linhasArq;
    }

    public void setLinhasArq(String linhasArq) {
        this.linhasArq = linhasArq;
    }

    public WorkerDyn() {
        this.param = new String();
        this.linhaComando = new String();
        this.linhasArq = new String();
        this.nomeFunc = new String();
    }


    
    public static void main(String[] args) throws FileNotFoundException {
        WorkerDyn s = new WorkerDyn();
        String result = new String();
        result =s.processaDyn("/home/renan/html.dyn");
        System.out.println(result);
    }
}

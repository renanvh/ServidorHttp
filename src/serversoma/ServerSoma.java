/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serversoma;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;

/**
 *
 * @author renan
 */
public class ServerSoma {

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        try {
            int serverPort = 6969; // porta do servidor
            /* cria um socket e mapeia a porta para aguardar conexao */
            ServerSocket listenSocket = new ServerSocket(serverPort);
            Telemetria telemetria = new Telemetria();

            while (true) {
                System.out.println("Servidor aguardando conexao ...");
                /* aguarda coneccoes */
                Socket clientSocket = listenSocket.accept();

                System.out.println("Cliente conectado ... Criando thread ...");
                /* cria um thread para atender a conexao */
                Connection c = new Connection(clientSocket, telemetria);
                c.contTelemetria();
                //System.out.println("TELEMETRIA");
                System.out.println(c.getTelemetria().getNumConexoes());
                System.out.println(c.getTelemetria().getTempo());
            } //while

        } catch (IOException e) {
            System.out.println("Listen socket:" + e.getMessage());
        } //catch
    } //main

}

class Connection extends Thread {

    Socket clientSocket;
    BufferedReader in;
    BufferedWriter out;
    String httpMethod;
    String resourcePath;
    HashMap requestHeaderMap;
    String respostaServer;
    Telemetria telemetria;

    public String getRespostaServer() {
        return respostaServer;
    }

    public void setRespostaServer(String respostaServer) {
        this.respostaServer = respostaServer;
    }

    public Socket getClientSocket() {
        return clientSocket;
    }

    public void setClientSocket(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public BufferedReader getIn() {
        return in;
    }

    public void setIn(BufferedReader in) {
        this.in = in;
    }

    public BufferedWriter getOut() {
        return out;
    }

    public void setOut(BufferedWriter out) {
        this.out = out;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public HashMap getRequestHeaderMap() {
        return requestHeaderMap;
    }

    public void setRequestHeaderMap(HashMap requestHeaderMap) {
        this.requestHeaderMap = requestHeaderMap;
    }

    public Connection(Socket c, Telemetria tel) throws IOException {
        clientSocket = c;
        in = new BufferedReader(new InputStreamReader(c.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(c.getOutputStream()));
        resourcePath = "";
        requestHeaderMap = new HashMap();
        httpMethod = new String();
        respostaServer = new String();
        this.start();
        this.telemetria = tel;

    }

    public Telemetria getTelemetria() {
        return telemetria;
    }

    public void setTelemetria(Telemetria telemetria) {
        this.telemetria = telemetria;
    }

    private void addTelemetriaNaMao() throws FileNotFoundException, IOException {
        // System.out.println("TA DOIDO");
        File tele = new File("src/htmls/telemetria.html");
        String html = new String();
        html = preparaTelemetria(tele);
        this.out.write("HTTP/1.1 200 OK\r\n");
        this.out.write(getRespostaServer());
        this.out.write("\r\n");
        this.out.flush();

        this.out.write(html);
        this.out.flush();
        //System.out.println("ENTREI");
    }

    private void processaExecutavel(String arquivo) throws IOException {
        File f = new File(getResourcePath());
        Process p = null;
        BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String linha = new String();
        String msgfinal = new String();

        boolean statusProcesso = false;
        if (f.canExecute()) {
            try {
                p = new ProcessBuilder(this.getResourcePath()).start();
                statusProcesso = true;
            } catch (Exception e) {
                statusProcesso = false;
            }

            if (statusProcesso) {
                while ((linha = input.readLine()) != null) {
                    msgfinal += linha;
                }
                System.out.println(msgfinal);
                this.out.write("HTTP/1.1 200 OK\r\n");
                this.out.flush();
                this.out.write(linha);
                this.out.flush();
                this.out.write("\r\n");
                this.out.flush();
            }
        }
    }

    private String alteraCookie() {
        String auxCookie = (String) this.getRequestHeaderMap().get("Cookie");
        String[] splitter;
        int cookieNum;

        if (auxCookie.contains("; ")) {
            splitter = auxCookie.split("; ");
            String[] ultimoCookie = splitter[0].split("=");
            cookieNum = Integer.parseInt(ultimoCookie[1]);
        } else {
            splitter = auxCookie.split("=");
            cookieNum = Integer.parseInt(splitter[1]);
        }
        return ("set-cookie: count=" + (cookieNum + 1) + "\r\n");
    }

    private String addCookie() {
        return ("set-cookie: count=0\r\n");
    }

    public void contTelemetria() {
        getTelemetria().setNumConexoes(getTelemetria().getNumConexoes() + 1);
    }

    private void metodoPost() throws IOException {
        //String listadefiles = new String();
        File f = new File(getResourcePath());
        Path caminhoDocSelecionado = Paths.get(getResourcePath());

        if (getResourcePath().startsWith("/virtual/feedback")) {
            StringBuilder stringBuilder = new StringBuilder();
            String comment = new String();
            try {
                if (in != null) {
                    int sizepost = Integer.parseInt((String) getRequestHeaderMap().get("Content-Length"));
                    char[] charBuffer = new char[sizepost];
                    //int bytesRead = -1;
                    in.read(charBuffer);
                    stringBuilder.append(charBuffer);
                }
            } catch (Exception e) {
                System.out.println("ERROR: " + e);
            }
            /*while ((bytesRead = in.read(charBuffer)) > 0) {
                    stringBuilder.append(charBuffer, 0, bytesRead);
                    System.out.println("TOAK");
                }
            } else{
                stringBuilder.append("");
            }*/

            comment = stringBuilder.toString();
            //System.out.println("AAA");
            String splitter[] = comment.split("=");

            salvaFeedback(splitter[1]);
            //this.num= this.num+1;

            /*File feedBack = new File(pathFeed);
            FileWriter writer = new FileWriter(feedBack);
            PrintWriter pw = new PrintWriter (writer);
            pw.print(splitter[1]);
            pw.close();*/
            //aux= aux+1;
            setResourcePath("");
            this.out.write("HTTP/1.1 301 Moved Permanently\r\n");
            addResposta("Location: " + getResourcePath() + "/\r\n");
            this.out.write(getRespostaServer());
            this.out.write("\r\n");
            this.out.flush();

        }

    }

    private void salvaFeedback(String texto) throws FileNotFoundException {
        String pathFeed = "/home/renan/NetBeansProjects/ServerSoma/src/feedback/" + "feedback" + ".txt";
        try {
            PrintWriter pw = new PrintWriter(new File(pathFeed));
            pw.write(texto);
            pw.close();
        } catch (Exception e) {
            System.out.println("ERRO AO SALVAR FEEDBACK: " + e);
        }
        //StringBuilder sb = new StringBuilder();
        //sb.append(splitter[1]);
    }

    private String preparaTelemetria(File f) throws FileNotFoundException {
        Scanner conteudo = new Scanner(f);
        String texto = new String();
        //String htmlTelemetria = new String();
        while (conteudo.hasNextLine()) {
            texto = texto.concat(conteudo.nextLine());
        }

        int horas = this.getTelemetria().getTempo().getHours();
        int minutos = this.getTelemetria().getTempo().getMinutes();
        int segundos = this.getTelemetria().getTempo().getSeconds();
        String tempoIniciado = horas + ":" + minutos + ":" + segundos;
        texto = texto.replaceAll("tempoinicio", tempoIniciado);

        /*Calendar cal = Calendar.getInstance();
        Date d = cal.getTime();
        int horasini = d.getHours();
        int minutosini = d.getMinutes();
        int segundosini = d.getSeconds();
        String tempoinit = (horasini - horas)+":"+ (minutosini-minutos)+":"+(segundosini-segundos);*/
        String tempoOnline = calcTempoInicial(this.getTelemetria().getTempo());

        texto = texto.replaceAll("tempoonline", tempoOnline);
        texto = texto.replaceAll("numconexoes", this.getTelemetria().getNumConexoes().toString());

        return texto;
    }

    private String calcTempoInicial(Date tempoInicial) {
        Calendar cal = Calendar.getInstance();
        Date d = cal.getTime();
        long duration  = d.getTime() -tempoInicial.getTime();
        long diffSegundos = TimeUnit.MILLISECONDS.toSeconds(duration);
        long diffMinutos = TimeUnit.MILLISECONDS.toMinutes(duration);
        long diffHoras = TimeUnit.MILLISECONDS.toHours(duration);
        
        /*long horasini = d.getHours();
        long minutosini = d.getMinutes();
        long segundosini = d.getSeconds();
        System.out.println("TEMPO AGORA: " +segundosini);
        

        String tempoOnline = (horasini - horas) + ":" + (minutosini - minutos) + ":" + (segundosini - segundos);
*/
        String tempoOnline = diffHoras%24+":"+diffMinutos%60+":"+diffSegundos%60;
        return tempoOnline;
    }

    private JSONObject montaJson() {
        JSONObject json = new JSONObject();
        int horas = this.getTelemetria().getTempo().getHours();
        int minutos = this.getTelemetria().getTempo().getMinutes();
        int segundos = this.getTelemetria().getTempo().getSeconds();
        String tempoIniciado = horas + ":" + minutos + ":" + segundos;
        String tempoOnline = calcTempoInicial(this.getTelemetria().getTempo());
        String numConexoes = this.getTelemetria().getNumConexoes().toString();

        json.put("tempoIniciado", tempoIniciado);
        json.put("tempoOnline", tempoOnline);
        json.put("numConexoes", numConexoes);

        return json;
    }

    private void escreveBufferJson(byte[] buffer) throws IOException {
        OutputStream data = new DataOutputStream(this.clientSocket.getOutputStream());
        data.write(buffer, 0, buffer.length);
        data.close();
        data.flush();
    }

    public enum HttpMethod {
        OPTIONS("opt"), GET("get"), HEAD("head"), POST("post"), PUT("put"), DELETE("del"),
        TRACE("trace"), CONNECT("conn");

        private String method;

        HttpMethod(String httpMethod) {
            this.method = httpMethod;
        }

        public String getMethod() {
            return method;
        }

    }

    private void escreveArq(File arq) throws FileNotFoundException, IOException {
        byte[] buffer = new byte[1024];
        int bytesRead;
        FileInputStream lerArq = new FileInputStream(arq);
        OutputStream fileout = new DataOutputStream(this.clientSocket.getOutputStream());

        while ((bytesRead = lerArq.read(buffer)) != -1) {

            fileout.write(buffer, 0, bytesRead);
            fileout.flush();

        }

        lerArq.close();
    }    
    
    private String escreveDir() {
        File f = new File(getResourcePath());
        File[] listafiles = f.listFiles();
        String files = new String();

        for (File x : listafiles) {
            files = files.concat("<tr><td><a href=\"" + x.getName() + "\">" + x.getName()
                    + "</a></td><td>" + x.length() + "</td></tr>\n");
        }

        String html = "<!DOCTYPE html>\n"
                + "<html>"
                + "<head>"
                + "<title>Index of " + getResourcePath() + "</title>"
                + "</head>"
                + "<body>"
                + "<h1>Index of " + getResourcePath() + "</h1>"
                + "<table>"
                + "<tr>\n"
                + "<th> File Name </th>\n"
                + "<th> Size </th>\n"
                + "</tr>\n"
                + files
                + "</table>"
                + "<address>Hoshi Server/0.1b Server at "+getRequestHeaderMap().get("Host")+"</address>"
                + "</body></html>";
        //html= html.trim();
        return html;
    }

    public void processaCabecalho() throws IOException {
        String msg = new String();
        boolean primeiraLinha = true;
        String[] splitter;

        while (!(msg = in.readLine()).equals("")) {
            System.out.println(msg);
            if (primeiraLinha) {
                splitter = msg.split(" ");
                this.setHttpMethod(splitter[0]);
                System.out.println(splitter[0]);
                this.setResourcePath(splitter[1]);
                System.out.println(this.getResourcePath());
                primeiraLinha = false;
            } else {
                splitter = msg.split(": ");
                this.requestHeaderMap.put(splitter[0], splitter[1]);
            }
        }
    }

    public boolean acessoAutorizado() throws UnsupportedEncodingException {
        if (this.requestHeaderMap.containsKey("Authorization")) {
            return verifAcesso();
        } else {
            return false;
        }
    }

    public boolean verifAcesso() throws UnsupportedEncodingException {
        String aux = (String) this.requestHeaderMap.get("Authorization");
        //System.out.println("AUX :  "+ aux);
        String[] splitter = aux.split("Basic ");
        byte[] decode = Base64.getDecoder().decode(splitter[1]);
        String login = new String(decode, "UTF-8");
        String[] splitter2 = login.split(":");

        return (splitter2[0].equals("admin") && splitter2[1].equals("admin"));
    }

    public void processaMetodo() throws IOException {
        switch (this.httpMethod) {
            case "GET":
                this.metodoGet();
            case "CONNECT":
                System.out.println("CONNECT");
                break;
            case "DELETE":
                System.out.println("DELETE");
                break;
            case "HEAD":
                System.out.println("HEAD");
                break;
            case "OPTIONS":
                System.out.println("OPTIONS");
                break;
            case "POST":
                this.metodoPost();
                System.out.println("POST");
                break;
            case "PUT":
                System.out.println("PUT");
                break;
            case "TRACE":
                System.out.println("TRACE");
                break;
            default:
                System.out.println("ERROR");
                break;
        }
    }

    public void metodoGet() throws IOException {

        String listadefiles = new String();
        File f = new File(getResourcePath());
        Path caminhoDocSelecionado = Paths.get(getResourcePath());

        if (f.isDirectory()) {
            if (this.acessoAutorizado()) {
                //System.out.println("PINTEI");
                System.out.println(getResourcePath().charAt(getResourcePath().length() - 1));
                if ((getResourcePath().charAt(getResourcePath().length() - 1)) != '/') {
                    //System.out.println("ENTREI AQUI NO COOKIE");
                    this.out.write("HTTP/1.1 301 Moved Permanently\r\n");
                    addResposta("Location: " + getResourcePath() + "/\r\n");
                    this.out.write(getRespostaServer());
                    this.out.write("\r\n");
                    this.out.flush();
                    /*
                    this.out.flush();
                    this.out.write("Location: " + getResourcePath() + "/\r\n");
                    this.out.flush();
                    this.out.write("\r\n");
                    this.out.flush();
                    this.out.write("\r\n");
                    this.out.flush();*/
                } else {
                    //System.out.println("entrei2");
                    this.out.write("HTTP/1.1 200 OK\r\n");
                    this.out.write(getRespostaServer());
                    this.out.write("\r\n");
                    this.out.flush();
                    listadefiles = escreveDir();
                    this.out.write(listadefiles);
                    this.out.flush();

                    /*this.out.flush();
                    this.out.write("\r\n");
                    this.out.flush();
//System.out.println("ENTRADO");
                    listadefiles = escreveDir();
                    //System.out.println(listadefiles);
                    this.out.write(listadefiles);
                    this.out.flush();*/
                    //this.out.write("\r\n");
                }
            } else {
                this.out.write("HTTP/1.1 401 Authorization Required\r\n");
                this.out.flush();
                this.out.write("WWW-Authenticate: Basic realm=\"User Visible Realm\"");
                this.out.write("\r\n");
                this.out.flush();
            }

        } else if (getResourcePath().contains(".dyn")) {
            //System.out.println("ENTREI");
            //System.out.println(getResourcePath());
            String resultdyn = new String();
            String linhasArq = new String();
            WorkerDyn wd = new WorkerDyn();
            resultdyn = wd.processaDyn(getResourcePath());
            //resultdyn = resultdyn.replace("<%", "");
            //resultdyn = resultdyn.replace("%>", "");
            this.out.write("HTTP/1.1 200 OK\r\n");
            this.addResposta("content-type: " + Files.probeContentType(caminhoDocSelecionado) + "\r\n");
            this.addResposta("content-lenght: " + f.length() + "\r\n");
            this.out.write(this.getRespostaServer());
            this.out.write("\r\n");
            this.out.flush();

            this.out.write(resultdyn);
            this.out.flush();

        } else if (getResourcePath().contains(".exe")) {
            Process p = null;
            boolean foiProcess = true;
            String teste = new String();
            String html = new String();

            try {
                p = new ProcessBuilder(this.getResourcePath()).start();
            } catch (Exception e) {
                foiProcess = false;
            }

            if (foiProcess) {
                BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line = new String();
                while ((line = input.readLine()) != null) {
                    teste = teste.concat(line);
                }
                html = "<!DOCTYPE html>\n"
                        + "<html>\n"
                        + "    <head>\n"
                        + "        <title>Executando o EXE maroto</title>\n"
                        + "        <meta charset=\"UTF-8\">\n"
                        + "        <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
                        + "    </head>\n"
                        + "    <body>\n"
                        + "        <div>\n"
                        + teste
                        + "        </div>\n"
                        + "    </body>\n"
                        + "</html>";
                this.out.write("HTTP/1.1 200 OK\r\n");
                this.out.write(this.getRespostaServer());
                this.out.write("\r\n");
                this.out.flush();
                this.out.write(html);
                this.out.flush();
            } else {
                this.out.write("HTTP/1.1 404 NOT FOUND\r\n");
                this.out.write("\r\n");
                this.out.flush();
            }

        } else if (getResourcePath().equals("/virtual/status/telemetria.html")) {

            addTelemetriaNaMao();
        } else if (getResourcePath().equals("/virtual/telemetria/status.json")) {
            JSONObject json =montaJson();
            byte[] buffer = json.toString().getBytes();
            this.out.write("HTTP/1.1 200 OK\r\n");
            addResposta("content-type: application/json\r\n");
            addResposta("content-lenght: "+buffer.length+"\r\n");
            addResposta("Access-Control-Allow-Origin: *\r\n");
            this.out.write(getRespostaServer());
            this.out.write("\r\n");
            this.out.flush();
            escreveBufferJson(buffer);
        } else if (getResourcePath().equals("/virtual/status/telemetria.js")) {
            File htmljs = new File("src/htmls/telemetriajs.html");
            Path pathjs = Paths.get(htmljs.getPath());
            addResposta("content-type: " + Files.probeContentType(pathjs) + "\r\n");
            addResposta("content-lenght: " + htmljs.length() + "\r\n");
            this.out.write("HTTP/1.1 200 OK\r\n");
            this.out.write(getRespostaServer());
            this.out.write("\r\n");
            this.out.flush();
            escreveArq(htmljs);

        } else if (f.isFile()) {
            //System.out.println("OI");
            //StringBuilder resposta = new StringBuilder();
            File arq = new File(getResourcePath());
            this.out.write("HTTP/1.1 200 OK\r\n");
            addResposta("content-type: " + Files.probeContentType(caminhoDocSelecionado) + "\r\n");
            addResposta("content-lenght: " + f.length() + "\r\n");
            this.out.write(getRespostaServer());
            //this.out.flush();
            this.out.write("\r\n");
            this.out.flush();
            escreveArq(arq);
        } else {
            //System.out.println("OI");
            this.out.write("HTTP/1.1 404 NOT FOUND\r\n");

            Path pathNotfound = Paths.get("/src/htmls/404.html");
            File notfound = new File("src/htmls/404.html");
            addResposta("content-type: " + Files.probeContentType(pathNotfound) + "\r\n");
            addResposta("content-lenght: " + notfound.length() + "\r\n");
            this.out.write(getRespostaServer());
            this.out.write("\r\n");
            this.out.flush();

            escreveArq(notfound);
        }
    }

    public boolean temCookie() {
        if (this.getRequestHeaderMap().containsKey("Cookie")) {
            return true;
        } else {
            return false;
        }
    }

    public void addResposta(String line) {
        if (this.getRespostaServer().isEmpty()) {
            this.setRespostaServer(line);
        } else {
            this.respostaServer = this.respostaServer.concat(line);
        }
    }

    @Override
    public void run() {

        //boolean serv = true;
        try {
            this.processaCabecalho();
            /*
            if(this.resourcePath.contains(".exe")){
            String[] splitter = this.resourcePath.split("/");
            
            System.out.println("OI"+splitter[0]);
            System.out.println("OI"+splitter[1]);
                if (splitter[splitter.length-1].contains(".exe")) {
                    System.out.println(splitter[splitter.length-1]);
                    //this.processaExecutavel(splitter[splitter.length-1]);
                }
            }*/

            if (temCookie()) {
                this.addResposta(alteraCookie());
            } else {
                this.addResposta(addCookie());
            }
            this.processaMetodo();

            this.in.close();
            this.out.close();
            this.clientSocket.close();

            //readline come o \n
            //System.out.println("teste");
            //String data = in.readUTF();
            //String resposta = "HTTP/1.1 200 OK\r\n\r\nhello world";
            //System.out.println("Cliente enviou: " + data);
            //if (data.contains("+")) {
            //resposta = "HTTP\\/1.1 200 TUDO OK";
            //checkSoma(data);
            //out.write(resposta);
            //out.writeBytes("\r\n");
            //out.close();
            //clientSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(Connection.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

        //out.writeUTF(resposta);
        //out.writeUTF("\\n");
        //}
    }

}

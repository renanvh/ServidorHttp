/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serversoma;

import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author renan
 */
public class Telemetria {
    Date tempo;
    Integer numConexoes;

    public Telemetria() {
        this.tempo = calcTempo();
        this.numConexoes = 0;
    }

    public Date getTempo() {
        return tempo;
    }

    public void setTempo(Date tempo) {
        this.tempo = tempo;
    }

    public Integer getNumConexoes() {
        return numConexoes;
    }

    public void setNumConexoes(Integer numConexoes) {
        this.numConexoes = numConexoes;
    }


    
    public Date calcTempo(){
        Calendar cal = Calendar.getInstance();

        Date date = cal.getTime();
        
        return date;
    }
}

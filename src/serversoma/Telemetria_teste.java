/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serversoma;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author renan
 */
public class Telemetria_teste {
 
    public static void main(String[] args) {
        Calendar cal = Calendar.getInstance();

        Date date = cal.getTime();

        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

        String formattedDate = dateFormat.format(date);

        System.out.println("Current time of the day using Calendar - 24 hour format: " + formattedDate);
    }
}

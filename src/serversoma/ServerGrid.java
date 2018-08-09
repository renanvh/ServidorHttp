/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serversoma;

import java.util.List;

/**
 *
 * @author renan
 */
public class ServerGrid extends Thread{
    int broadcast = 5554;
    int respUnicast = 6666;
    List<Servers> servers;
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.amis.speedyjoes.view.push;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.event.Observes;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;


import nl.amis.speedyjoes.common.log.ConversationLogger;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import org.sample.whiteboardapp.TimeEvent;
import org.sample.whiteboardapp.WBTimeEvent;


@Singleton
@ServerEndpoint("/mediatorendpoint")
public class SocketMediator {

    private static Set<Session> peers = Collections.synchronizedSet(new HashSet<Session>());
    private static Map<String, Session> peerTableMap = Collections.synchronizedMap(new HashMap<String,Session>());

    @OnMessage
    public String onMessage(String message, Session session) {
        System.out.println("message received " + message);
        String tableNumber= "";
        JSONObject jObj;
        try {
            jObj = new JSONObject(message);
            tableNumber= (String) jObj.get("tableNumber");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // link up session with tableNumber so we know which session to inform when a meal item ready event occurs for that table
        peerTableMap.put(tableNumber, session);
//
//        for (Session peer : peers) {
//            if (!peer.equals(session)) {
//                try {
//                    peer.getBasicRemote().sendText(message + " - retweet");
//                } catch (IOException ex) {
//                    Logger.getLogger(SocketMediator.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//        }
        return "message was received and processed: "+message;
    }

    @OnOpen
    public void onOpen(Session peer) {
        peers.add(peer);
    }

    public void onTimeEvent(@Observes @WBTimeEvent TimeEvent event) {
        System.out.println("Time Event observed by SocketMediator " + event.getTimestamp());
        for (Session peer : peers) {
            try {
                peer.getBasicRemote().sendText("Time event: " + event.getTimestamp());
            } catch (IOException ex) {
                Logger.getLogger(SocketMediator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }// onTimeEvent

 
    public void onMailItemReadyEvent(@Observes @JoesMealItemReadyEvent MealItemReadyEvent event) {
        System.out.println("Meal Item Ready Event observed by SocketMediator " + event.getAppetizer()+" for table "+event.getTableNumber());
        ConversationLogger logger = new ConversationLogger(event.getJsonTrace());
        logger.enterLog("Waiter", logger.getHighestLevel()+1, "Taken cooked meal item to table ", 500);

        String json="{}";
        try {
            JSONObject r = new JSONObject().put("appetizerOrMain", event.getAppetizerOrMain());
            r.put("menuItem", event.getMenuItem());
            r.put("price", event.getPrice());
            r.put("duration", event.getDuration());
            r.put("trace", logger.toJSON());
             json = r.toString();
            System.out.println("response=" + r);
        } catch (JSONException e) {
        }

        
        if (peerTableMap.containsKey(event.getTableNumber())) {
        Session s = peerTableMap.get(event.getTableNumber());
            try {
                s.getBasicRemote().sendText(json );
            } catch (IOException e) {
            }
        }
    }//onMailItemReadyEvent

    
    @OnClose
    public void onClose(Session peer) {
        peers.remove(peer);
    }
}


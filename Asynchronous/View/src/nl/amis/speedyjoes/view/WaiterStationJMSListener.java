package nl.amis.speedyjoes.view;

import javax.ejb.MessageDriven;

import javax.enterprise.event.Event;

import javax.inject.Inject;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;

import javax.jms.MessageListener;

import nl.amis.speedyjoes.view.push.JoesMealItemReadyEvent;

import nl.amis.speedyjoes.view.push.MealItemReadyEvent;

import org.sample.whiteboardapp.TimeEvent;
import org.sample.whiteboardapp.WBTimeEvent;


@MessageDriven(mappedName = "jms/JoesQueue") 
public class WaiterStationJMSListener implements MessageListener{
    @Inject  @JoesMealItemReadyEvent Event<MealItemReadyEvent> mealItemReadyEvent;
    
    public WaiterStationJMSListener() {
        super();
    }
@Override
    public void onMessage(Message message) {
            try {
                // In JMS 1.1:
                          MapMessage mapMessage = (MapMessage)message;
                System.out.println("JMS CONSUMER!!!! Message received: " + mapMessage);
                System.out.println("Menu item"+mapMessage.getString("menuItem"));
                
                MealItemReadyEvent event = new MealItemReadyEvent();
                event.setMenuItem(mapMessage.getString("menuItem"));
                event.setAppetizerOrMain(mapMessage.getString("AorM"));
                event.setTableNumber(mapMessage.getString("tableNumber"));
                event.setPrice(mapMessage.getFloat("price"));
                event.setDuration(mapMessage.getInt("duration"));
                
                mealItemReadyEvent.fire(event);        

                
            } catch (JMSException e) {
                System.err.println("Error while fetching message payload: " + e.getMessage());
            }
        }
}

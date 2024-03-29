/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.utbm.gi.vi51.project.agent;

import static fr.utbm.gi.vi51.project.agent.FestivalEntity.EN_ATTENTE;
import static fr.utbm.gi.vi51.project.agent.FestivalEntity.MARCHE_VERS_CONCERT;
import static fr.utbm.gi.vi51.project.agent.FestivalEntity.MARCHE_VERS_DECHET;
import static fr.utbm.gi.vi51.project.agent.FestivalEntity.MARCHE_VERS_DESTINATION;
import static fr.utbm.gi.vi51.project.agent.FestivalEntity.MARCHE_VERS_NOURRITURE;
import static fr.utbm.gi.vi51.project.agent.FestivalEntity.MARCHE_VERS_TOILETTES;
import static fr.utbm.gi.vi51.project.agent.FestivalEntity.RUN_AWAY;
import static fr.utbm.gi.vi51.project.agent.FestivalEntity.WANDERING;
import fr.utbm.gi.vi51.project.environment.Scene;
import fr.utbm.gi.vi51.project.environment.WaterClosed;
import fr.utbm.gi.vi51.project.environment.food.Food;
import fr.utbm.gi.vi51.project.utils.RandomUtils;
import java.util.ArrayList;
import java.util.Collection;
import org.arakhne.afc.math.discrete.object2d.Point2i;
import org.janusproject.jaak.envinterface.body.TurtleBody;
import org.janusproject.jaak.envinterface.body.TurtleBodyFactory;
import org.janusproject.jaak.envinterface.frustum.SquareTurtleFrustum;
import org.janusproject.jaak.envinterface.perception.EnvironmentalObject;
import org.janusproject.jaak.envinterface.perception.Perceivable;
import org.janusproject.jaak.envinterface.time.JaakTimeManager;
import org.janusproject.jaak.turtle.Turtle;
import org.janusproject.kernel.message.Message;
import org.janusproject.kernel.message.ObjectMessage;
import org.janusproject.kernel.message.StringMessage;

/**
 *
 * @author Benjamin
 */
public class FestivalTrashMan extends FestivalEntity {
    
    /**
     *
     */
    private static final long serialVersionUID = 1021603672862125311L;
    
    
    private TurtleSemantic _informationsTurtle;
    
    private Food _trashFounded;
    
    private long _timeSincePreviousAction;
    
    
    
    public FestivalTrashMan() {
        super();
        _informationsTurtle = new TurtleSemantic(this);
        _frustum = new SquareTurtleFrustum(10);
        changeCurrentState(INIT);
    }
    
    @Override
    protected TurtleBody createTurtleBody(TurtleBodyFactory factory) {
        
        TurtleBody turtleBody = factory.createTurtleBody(getAddress());
        turtleBody.setSemantic(_informationsTurtle);
        
        return turtleBody;
    }
    
    @Override
    protected void turtleBehavior() {
            
        JaakTimeManager jaakTimeManager = getJaakTimeManager();
        
        _timeSincePreviousAction += jaakTimeManager.getWaitingDuration();
        
        
        _isOnSamePosition = getPosition().equals(_previousPosition);
        _previousPosition = getPosition();
        if(checkFailMove()) return;
        
       // System.out.println(""+getPosition());
        
        Collection<Perceivable> perception = getPerception();
        
        
        Message m = this.getMessage();
        if(m instanceof ObjectMessage) {
        	if(((ObjectMessage)m).getContent() instanceof Point2i) {
        		this._currentDestination=((Point2i)((ObjectMessage)m).getContent());
        		changeCurrentState(RUN_AWAY);
        	}
        }

		if(getCurrentState()==RUN_AWAY) {
			//on prévient les agents que l'on fuit
			for(Perceivable p : perception) {
				if (p.isTurtle()) {
					TurtleSemantic t = (TurtleSemantic)p.getSemantic();
					this.forwardMessage(new ObjectMessage(this._currentDestination),t.getOwner().getAddress());
				}
			}
			runAway();
			return;
		}
        
        
            for(Perceivable tmpObj : perception)
            {
                //System.out.println("tmpObj : "+tmpObj);
                if(tmpObj instanceof Food)
                {
                    if(_trashFounded == null)// || (_trashFounded != null && tmpObj.getPosition().distance(getPosition()) < _trashFounded.getPosition().distance(getPosition())))
                    {
                        System.out.println("FOOOOD  "+tmpObj+" "+tmpObj.getPosition());
                        _trashFounded = (Food)tmpObj;
                        changeCurrentState(MARCHE_VERS_DECHET);
                        _currentDestination = _trashFounded.getPosition();
                        _currentConstructDestination = null;
                    }
                }
            }
        
        if(_trashFounded == null)
        {
            
        }
        else
        {
            
            if(arrived())
            {
                System.out.println("PICK FOOD");
                pickUp(_trashFounded);
                _trashFounded = null;
                _currentDestination = null;
                chooseToGoToARandomDestination();
            }

            
        }
        //System.out.println("state : "+_currentState);
        
        switch(getCurrentState())
        {
            case INIT:
                chooseToGoToARandomDestination();
                break;
                
            case MARCHE_VERS_CONCERT:
            case MARCHE_VERS_TOILETTES:
            case MARCHE_VERS_NOURRITURE:
            case MARCHE_VERS_DESTINATION:     
                if(_timeSincePreviousAction > 10*1000)
                {
                    _timeSincePreviousAction = 0;
                    changeCurrentState(WANDERING);
                }
                
             case MARCHE_VERS_DECHET:    
                moveToDestination();
                
                
                break;
            case WANDERING:   
            default:
                this.setHeading(this.getHeadingAngle()+RandomUtils.randomBinomial((float)Math.PI/4));
                moveForward(1);
                if(_timeSincePreviousAction > 10*1000)
                {
                    _timeSincePreviousAction = 0;
                    chooseToGoToARandomDestination();
                }
        }
        
        
        
        
        
        
        
    }
}
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import engine.Car;
import engine.Game;
import engine.GameObject;
import utilities.MyVector;

/**
 *
 * @author santi
 * @modifier Hoa
 */
public class SeekController extends Controller {

    GameObject target;

    public SeekController(GameObject target) {
        this.target = target;
    }

    public void update(Car subject, Game game, double delta_t, double[] controlVariables) {
        MyVector carVelocity = getCarVelocity(subject);
        MyVector A = getSteeringBehavior(subject);
        carVelocity = carVelocity.divide(carVelocity.getLength());
        double projection = A.dot(carVelocity);
        if (projection < Math.pow(10, -10))
            projection = 0;

        if (projection > 0) {
            if (carVelocity.get(0) > 0) {
                controlVariables[VARIABLE_THROTTLE] = Math.abs(projection)/100;
                controlVariables[VARIABLE_BRAKE] = 0;
            } else {
                controlVariables[VARIABLE_THROTTLE] = 0;
                controlVariables[VARIABLE_BRAKE] = Math.abs(projection)/100;
            }
        } else if (projection < 0) {
            if (carVelocity.get(0) > 0) {
                controlVariables[VARIABLE_THROTTLE] = 0;
                controlVariables[VARIABLE_BRAKE] = Math.abs(projection)/100;
            } else {
                controlVariables[VARIABLE_THROTTLE] = Math.abs(projection)/100;
                controlVariables[VARIABLE_BRAKE] = 0;
            }
        } else {
            controlVariables[VARIABLE_THROTTLE] = 0;
            controlVariables[VARIABLE_BRAKE] = 0;
        }

        MyVector carVelocityRotated = new MyVector(carVelocity.get(1), -carVelocity.get(0));
        projection = A.dot(carVelocityRotated);
        if (projection > 0)
            controlVariables[Controller.VARIABLE_STEERING] = +1;
        else if (projection < 0)
            controlVariables[Controller.VARIABLE_STEERING] = -1;
        else
            controlVariables[Controller.VARIABLE_STEERING] = 0;

        if (controlVariables[Controller.VARIABLE_STEERING] != 0 && controlVariables[VARIABLE_THROTTLE] == 0 && controlVariables[VARIABLE_BRAKE] == 0) {
            controlVariables[VARIABLE_THROTTLE] = 1;
            controlVariables[VARIABLE_BRAKE] = 0;
        }
    }
    
    private MyVector getSteeringBehavior(Car subject) {
        //find velocity to the target
        MyVector direction = getVector(target).minus(getVector(subject)).multiply(-1);
        direction = direction.divide(direction.getLength());
        direction = direction.multiply(100);

        return direction;
    }

    private MyVector getCarVelocity(Car subject) {
        double y = Math.sin(subject.getAngle());
        double x = Math.cos(subject.getAngle());
        if (subject.getSpeed() != 0) {
            y *= subject.getSpeed();
            x *= subject.getSpeed();
        }
        MyVector temp = new MyVector(x, y);

        return temp;
    }

    private MyVector getVector(GameObject subject) {
        MyVector temp = new MyVector(subject.getX(), subject.getY());

        return temp;
    }
}

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
public class ArriveController extends Controller {

    GameObject target;

    double targetRadius = 5;
    double slowRadius = 400;

    public ArriveController(GameObject target) {
        this.target = target;
    }

    public void update(Car subject, Game game, double delta_t, double[] controlVariables) {
        MyVector carVelocity = getCarVelocity(subject);
        MyVector A = getSteeringBehavior(subject, delta_t);
        carVelocity = carVelocity.divide(carVelocity.getLength());
        double projection = A.dot(carVelocity);

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

        MyVector carVelocityRotated = new MyVector(-carVelocity.get(1), carVelocity.get(0));
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
    
    private MyVector getSteeringBehavior(Car subject, double time) {
        MyVector direction = getVector(target).minus(getVector(subject));
        double length = direction.getLength();

        if (length < targetRadius) 
            return getCarVelocity(subject).multiply(-1);

        double targetSpeed;
        if (length > slowRadius)
            targetSpeed = subject.getMaxSpeed();
        else
            targetSpeed = subject.getMaxSpeed() * length/slowRadius;
        MyVector targetVelocity = direction.divide(length).multiply(targetSpeed);
        MyVector charVelocity = getCarVelocity(subject);
        direction = targetVelocity.minus(charVelocity).divide(time);

        if (Math.sqrt(direction.dot(direction)) > 100)
            direction = direction.divide(Math.sqrt(direction.dot(direction))).multiply(100);

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

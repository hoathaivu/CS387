/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import engine.Car;
import engine.Game;
import engine.GameObject;
import engine.RotatedRectangle;
import utilities.MyVector;
import java.util.Arrays;

/**
 *
 * @author santi
 * @modifier Hoa
 */
public class AdvSeekController extends Controller {

    GameObject target;

    public AdvSeekController(GameObject target) {
        this.target = target;
    }

    public void update(Car subject, Game game, double delta_t, double[] controlVariables) {
        MyVector carVelocity = getCarVelocity(subject);
        MyVector A = getSteeringBehavior(subject);
        A = doWallAvoidance(subject, game, A);
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
        MyVector direction = getVector(target).minus(getVector(subject)).multiply(-1);
        direction = direction.divide(direction.getLength());
        direction = direction.multiply(100);

        return direction;
    }

    private MyVector getSteeringBehavior(Car subject, double x, double y) {
        MyVector target = new MyVector(x, y);
        MyVector direction = target.minus(getVector(subject)).multiply(-1);
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

    private MyVector doWallAvoidance(Car subject, Game game, MyVector A) {
        MyVector carVelocity = getCarVelocity(subject);
        double vectorAngle = Math.PI - Math.acos(A.dot(carVelocity) / (A.getLength() * carVelocity.getLength()));

        RotatedRectangle box = subject.getCollisionBox();
        double pos = A.dot(new MyVector(-carVelocity.get(1), carVelocity.get(0)));
        double angle = 0;
        if (pos > 0)
            if (Math.toDegrees(subject.getAngle()) < 0)
                angle = subject.getAngle() - vectorAngle;
            else
                angle = subject.getAngle() + vectorAngle;
        else if (pos < 0)
            if (Math.toDegrees(subject.getAngle()) < 0)
                angle = subject.getAngle() + vectorAngle;
            else
                angle = subject.getAngle() - vectorAngle;
        else
            angle = subject.getAngle();
        box.ang = angle;
        boolean hit = rayCast(game, subject, box, angle);

        box.ang = subject.getAngle();
        if (!hit)
            return A;
        else {
            double[] safePoints = findSafeSpot(subject);
            MyVector A1 = trySafeSpots(game, subject, safePoints, carVelocity);
            if (A1 != null)
                return A1;
            else
                return A;
        }
    }

    private MyVector trySafeSpots(Game game, Car subject, double[] safePoint, MyVector carVelocity) {
        MyVector A;
        double vectorAngle, pos, angle;
        RotatedRectangle box;
        boolean hit;
        for (int i = 0; i < safePoint.length; i+=2) {
            A = getSteeringBehavior(subject, safePoint[i] , safePoint[i + 1]);
            vectorAngle = Math.PI - Math.acos(A.dot(carVelocity) / (A.getLength() * carVelocity.getLength()));
            box = subject.getCollisionBox();
            pos = A.dot(new MyVector(-carVelocity.get(1), carVelocity.get(0)));
            angle = 0;
            if (pos > 0)
                if (Math.toDegrees(subject.getAngle()) < 0)
                    angle = subject.getAngle() - vectorAngle;
                else
                    angle = subject.getAngle() + vectorAngle;
            else if (pos < 0)
                if (Math.toDegrees(subject.getAngle()) < 0)
                    angle = subject.getAngle() + vectorAngle;
                else
                    angle = subject.getAngle() - vectorAngle;
            else
                angle = subject.getAngle();
            box.ang = angle;
            hit = rayCast(game, subject, box, angle);

            box.ang = subject.getAngle();
            if (!hit)
                return A;
        }

        return null;
    }

    private boolean rayCast(Game game, Car subject, RotatedRectangle box, double angle) {
        double oldX = box.C.x, oldY = box.C.y;
        for (int i = 0; i < 100; i++) {
            box.C.x += Math.cos(angle);
            box.C.y += Math.sin(angle);
            if (game.collision(box) != null && game.collision(box) != target && game.collision(box) != subject) {
                box.C.x = oldX;
                box.C.y = oldY;
                return true;
            }
        }
        box.C.x = oldX;
        box.C.y = oldY;

        return false;
    }

    private double[] findSafeSpot(Car subject) {
        double x = subject.getX(), y = subject.getY();
        double x1 = target.getX(), y1 = target.getY();
        double[] p1 = {375 - subject.getImage().getWidth()/2 - 10, 150 - subject.getImage().getWidth()/2 - 1}; //top left
        double[] p2 = {375 + 50 + subject.getImage().getWidth()/2 + 10, 150 - subject.getImage().getWidth()/2 - 1}; //top right
        double[] p3 = {375 - subject.getImage().getWidth()/2 - 10, 150 + 300 + subject.getImage().getWidth()/2 + 1}; //bot left
        double[] p4 = {375 + 50 + subject.getImage().getWidth()/2 + 10, 150 + 300 + subject.getImage().getWidth()/2 + 1}; //bot right
        
        double dist1 = Math.sqrt(Math.pow(x - p1[0], 2) + Math.pow(y - p1[1], 2)) + Math.sqrt(Math.pow(x1 - p1[0], 2) + Math.pow(y1 - p1[1], 2));
        double dist2 = Math.sqrt(Math.pow(x - p2[0], 2) + Math.pow(y - p2[1], 2)) + Math.sqrt(Math.pow(x1 - p2[0], 2) + Math.pow(y1 - p2[1], 2));
        double dist3 = Math.sqrt(Math.pow(x - p3[0], 2) + Math.pow(y - p3[1], 2)) + Math.sqrt(Math.pow(x1 - p3[0], 2) + Math.pow(y1 - p3[1], 2));
        double dist4 = Math.sqrt(Math.pow(x - p4[0], 2) + Math.pow(y - p4[1], 2)) + Math.sqrt(Math.pow(x1 - p4[0], 2) + Math.pow(y1 - p4[1], 2));

        double[] temp = {dist1, dist2, dist3, dist4};
        Arrays.sort(temp);
        double[] result = new double[8];

        //I'm lazy right here. Just adding points based on the distances' order
        if (dist1 == temp[0]) {
            result[0] = p1[0];
            result[1] = p1[1];
        }
        if (dist2 == temp[0]) {
            result[0] = p2[0];
            result[1] = p2[1];
        }
        if (dist3 == temp[0]) {
            result[0] = p3[0];
            result[1] = p3[1];
        }
        if (dist4 == temp[0]) {
            result[0] = p4[0];
            result[1] = p4[1];
        }

        if (dist1 == temp[1]) {
            result[2] = p1[0];
            result[3] = p1[1];
        }
        if (dist2 == temp[1]) {
            result[2] = p2[0];
            result[3] = p2[1];
        }
        if (dist3 == temp[1]) {
            result[2] = p3[0];
            result[3] = p3[1];
        }
        if (dist4 == temp[1]) {
            result[2] = p4[0];
            result[3] = p4[1];
        }

        if (dist1 == temp[2]) {
            result[4] = p1[0];
            result[5] = p1[1];
        }
        if (dist2 == temp[2]) {
            result[4] = p2[0];
            result[5] = p2[1];
        }
        if (dist3 == temp[2]) {
            result[4] = p3[0];
            result[5] = p3[1];
        }
        if (dist4 == temp[2]) {
            result[4] = p4[0];
            result[5] = p4[1];
        }

        if (dist1 == temp[3]) {
            result[6] = p1[0];
            result[7] = p1[1];
        }
        if (dist2 == temp[3]) {
            result[6] = p2[0];
            result[7] = p2[1];
        }
        if (dist3 == temp[3]) {
            result[6] = p3[0];
            result[7] = p3[1];
        }
        if (dist4 == temp[3]) {
            result[6] = p4[0];
            result[7] = p4[1];
        }

        return result;
    }
}

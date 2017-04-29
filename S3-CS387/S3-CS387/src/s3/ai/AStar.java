package s3.ai;


import java.util.List;
import java.util.ArrayList;
import s3.base.S3;
import s3.entities.S3PhysicalEntity;
import s3.util.Pair;
import s3.util.ANode;

public class AStar {
	
	public static int pathDistance(double start_x, double start_y, double goal_x, double goal_y, S3PhysicalEntity i_entity, S3 the_game) {
		AStar a = new AStar(start_x,start_y,goal_x,goal_y,i_entity,the_game);
		List<Pair<Double, Double>> path = a.computePath();
		if (path!=null) return path.size();
		return -1;
	}

	private List<Pair<Double, Double>> path;

	public AStar(double start_x, double start_y, double goal_x, double goal_y, S3PhysicalEntity i_entity, S3 the_game) {
		//check if moving out-of-bound
		if (goal_x < 0 || goal_y < 0)
			return;
		//check if already at goal
		else if (start_x == goal_x && start_y == goal_y) {
			path = new ArrayList<Pair<Double, Double>>();
			return;
		}

		//clone to test for collision
		S3PhysicalEntity entity = (S3PhysicalEntity) i_entity.clone();

		// A*
		//initialize the open list
		ArrayList<ANode> open = new ArrayList<ANode>();
		//initialize the closed list
		ArrayList<ANode> close = new ArrayList<ANode>();
		ANode startp = new ANode(null, (int)start_x, (int)start_y, 0, 0, 0);
		open.add(startp);

		//while the open list is not empty
		boolean found = false;
		while (open.size() > 0) {
			//find the node with the least f on the open list, call it q
			//pop q off the open list
			ANode q = open.remove(findMinIndex(open));
			//generate q's successors and set their parents to q
			List<ANode> successors = getNextPossibleMoves(the_game, entity, q, goal_x, goal_y);
			//if successor is the goal, stop the search
			for (int i = 0; i < successors.size(); i++) {
				ANode cur = successors.get(i);
				if (cur.x == goal_x && cur.y == goal_y) {
					found = true;
					break;
				} else {
				//if no other node at same position as successor in 
				//OPEN and CLOSED list which has a lower f than 
				//successor, add successor to open
                                	if (!foundDuplicateBetterF(cur, open) && !foundDuplicateBetterF(cur, close))
                                		open.add(cur);
				}
			}
			//push q on the closed list
			close.add(q);

			//if reach goal, rebuild path and stop
			if (found) {
				path = new ArrayList<Pair<Double, Double>>();

				//get last node
				ANode cur_step = close.get(close.size() - 1);
				path.add(new Pair<Double, Double>((double) goal_x, (double) goal_y));
				//while current step is not the start
				while (cur_step.parent != null) {
					//System.out.println(the_game.mapEntityAt(cur_step.x, cur_step.y).toString());
					path.add(0, new Pair<Double, Double>((double) cur_step.x, (double) cur_step.y));
					cur_step = cur_step.parent;
				}

				break;
			}
		}
	}

	private double getManhattanDist(double start_x, double start_y, double goal_x, double goal_y) {
		return Math.abs(start_x - goal_x) + Math.abs(start_y - goal_y);
	}

	private int findMinIndex(ArrayList<ANode> list) {
		int min = 0;
		for (int i = 1; i < list.size(); i++)
			if (list.get(i).f < list.get(min).f)
				min = i;

		return min;
	}

	private List<ANode> getNextPossibleMoves(S3 the_game, S3PhysicalEntity entity, ANode q, double goal_x, double goal_y) {
		List<ANode> moves = new ArrayList<ANode>();

		//move up
		ANode up = new ANode(q, q.x, q.y - 1, 0, q.g + 1, getManhattanDist(q.x, q.y - 1, goal_x, goal_y));
		up.f = up.g + up.h;
		moves.add(up);
		//move down
		ANode down = new ANode(q, q.x, q.y + 1, 0, q.g + 1, getManhattanDist(q.x, q.y + 1, goal_x, goal_y));
		down.f = down.g + down.h;
		moves.add(down);
		//move left
		ANode left = new ANode(q, q.x - 1, q.y, 0, q.g + 1, getManhattanDist(q.x - 1, q.y, goal_x, goal_y));
		left.f = left.g + left.h;
		moves.add(left);
		//move right
		ANode right = new ANode(q, q.x + 1, q.y, 0, q.g + 1, getManhattanDist(q.x + 1, q.y, goal_x, goal_y));
		right.f = right.g + right.h;
		moves.add(right);

		//check if out-of-bound
		int i = 0;
		while (i < moves.size())
			if (moves.get(i).x < 0 || moves.get(i).y < 0)
				moves.remove(i);
			else
				i++;

		//check collision
		i = 0;
		while (i < moves.size()) {
			ANode move = moves.get(i);
			entity.setX(move.x);
			entity.setY(move.y);
			if (the_game.anyLevelCollision(entity) != null)
				moves.remove(i);
			else
				i++;
		}

		return moves;
	}

	private boolean foundDuplicateBetterF(ANode node, List<ANode> list) {
		for (int i = 0; i < list.size(); i++) {
			ANode list_cur = list.get(i);
			if (list_cur.x == node.x && list_cur.y == node.y)
				if (list_cur.f <= node.f)
					return true;
		}

		return false;
	}

	public List<Pair<Double, Double>> computePath() {
		return path;
	}

}

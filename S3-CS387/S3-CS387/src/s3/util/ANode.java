package s3.util;

public class ANode {
	public ANode parent;
	public int x, y;
        public double f, g, h;
	
	public ANode(ANode parent, int x, int y, double f, double g, double h) {
		this.parent = parent;
		this.x = x;
		this.y = y;
		this.f = f;
		this.g = g;
		this.h = h;
	}
}

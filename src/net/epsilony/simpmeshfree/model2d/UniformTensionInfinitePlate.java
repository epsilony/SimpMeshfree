/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model2d;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import net.epsilony.simpmeshfree.model.Boundary;
import net.epsilony.simpmeshfree.model.BoundaryCondition;
import net.epsilony.simpmeshfree.model.LineBoundary;
import net.epsilony.simpmeshfree.model.Node;
import net.epsilony.simpmeshfree.model.VolumeCondition;
import net.epsilony.simpmeshfree.model.WeakformProblem;
import net.epsilony.simpmeshfree.utils.QuadratureDomain;
import net.epsilony.simpmeshfree.utils.QuadratureDomains;
import net.epsilony.simpmeshfree.utils.QuadraturePointIterator;
import net.epsilony.simpmeshfree.utils.QuadraturePointIterators;
import net.epsilony.utils.geom.Coordinate;
import net.epsilony.utils.geom.GeometryMath;
import net.epsilony.utils.geom.Quadrangle;

/**
 *
 * @author epsilon
 */
public class UniformTensionInfinitePlate{

    double a, d, S, E, mu;
    int n_c, n_t;
    double q;
    private Node[] vertes = new Node[5];   //p_0,...,p_4
    private ArrayList<Node> p_Cs, p_Bs;
    int n_phi;
    ArrayList<ArrayList<Node>> nds_ij;
    ArrayList<QuadratureDomains.Quad> quads;
    ArrayList<LineBoundary> bnds;
    ArrayList<ArrayList<LineBoundary>> d_r_u_l_c_bnds;
    public static final int DOWN_LINE_INDEX = 0, RIGHT_LINE_INDEX = 1, UP_LINE_INDEX = 2, LEFT_LINE_INDEX = 3, CIRCLE_INDEX = 4;

    public UniformTensionInfinitePlate(double a, double d, double S, double E, double mu, int n_c, int n_t, double q) {
        this.a = a;
        this.d = d;
        this.S = S;
        this.E = E;
        this.mu = mu;
        this.n_c = n_c;
        this.n_t = n_t;
        this.q = q;
        init();
    }

    public Node[] getVertes() {
        return vertes;
    }

    public ArrayList<QuadratureDomain> getQuadratureDomains() {
        return new ArrayList<QuadratureDomain>(quads);
    }
    
    public ArrayList<Quadrangle> getQuadrangles(){
        ArrayList<Quadrangle> results=new ArrayList<>(quads.size());
        for( QuadratureDomains.Quad qd:quads){
            results.add(qd.getQuadrangle());
        }
        return results;
    }

    public ArrayList<LineBoundary> getBoundaries() {
        return bnds;
    }

    private void init() {
        if (n_c < 3) {
            n_c = 3;
        } else {
            int i_ = (int) Math.ceil((n_c - 3) / 2.0);
            n_c = 3 + 2 * i_;
        }
        n_t = n_t < 2 ? 2 : n_t;

        n_phi = (n_c + 1) / 2;
        genVertesNodes();
        genNodesOnCurveRightAndTop();
        genBoundries();
    }

    private void genVertesNodes() {
        double[] xys = new double[]{a, 0, d, 0, d, d, 0, d, 0, a};
        for (int i = 0; i < 5; i++) {
            vertes[i] = new Node(xys[i * 2], xys[i * 2 + 1]);
        }
    }

    private void genNodesOnCurveRightAndTop() {
        p_Cs = new ArrayList<>(n_c);
        p_Cs.add(vertes[0]);
        double r = a;
        for (int i = 1; i < n_c - 1; i++) {
            double theta = Math.PI /2/ (n_c - 1) * i;
            double x = r * Math.cos(theta);
            double y = r * Math.sin(theta);
            p_Cs.add(new Node(x, y));
        }
        p_Cs.add(vertes[4]);

        p_Bs = new ArrayList<>(n_c);
        p_Bs.add(vertes[1]);
        for (int i = 1; i < n_phi - 1; i++) {
            double theta=Math.PI/2/(n_c-1)*i;
            p_Bs.add(new Node(d, d*Math.tan(theta)));
        }
        p_Bs.add(vertes[2]);
        for (int i = n_phi; i < n_c - 1; i++) {
            double theta=Math.PI/2/(n_c-1)*i;
            p_Bs.add(new Node(d/Math.tan(theta), d));
        }
        p_Bs.add(vertes[3]);
        genNds_ij();
        genBoundries();
        genQuadratureDomains();
    }

    private double getInitLength(Node p_Ci, Node p_Bi) {
        double sumLen = GeometryMath.distance(p_Ci, p_Bi);
        return sumLen * (q - 1) / (Math.pow(q, n_t - 1) - 1);
    }

    private void genNds_ij() {
        nds_ij = new ArrayList<>(n_c);

        for (int i = 0; i < n_c; i++) {
            ArrayList<Node> nds_j = new ArrayList<>(n_t);
            nds_ij.add(nds_j);
            nds_j.add(p_Cs.get(i));
            double theta = (Math.PI /2/ (n_c - 1)) * i;
            double l_ = getInitLength(p_Cs.get(i), p_Bs.get(i));
            double r=a;
            for (int j = 1; j < n_t - 1; j++) {
                r+=l_;
                l_ *= q;
                double x = r * Math.cos(theta);
                double y = r * Math.sin(theta);
                if(i==n_c-1){
                    x=0;
                }
                nds_j.add(new Node(x, y));
            }
            nds_j.add(p_Bs.get(i));
        }
    }
    
    public ArrayList<Node> getSpaceNodes(){
        ArrayList<Node> result=new ArrayList<>((n_c-2)*(n_t-2));
        for(int i=1;i<n_c-1;i++){
            for(int j=1;j<n_t-1;j++){
                result.add(nds_ij.get(i).get(j));
            }
        }
        return result;
    }

    public double getE() {
        return E;
    }

    public double getMu() {
        return mu;
    }

    @SuppressWarnings("unchecked")
    private void genQuadratureDomains() {
        quads = new ArrayList((n_c - 1) * (n_t - 1));
        double[] points = new double[8];
        int[] ijShift = new int[]{0, 0, 0, 1, 1, 1, 1, 0};
        for (int i = 0; i < n_c - 1; i++) {
            for (int j = 0; j < n_t - 1; j++) {
                for (int k = 0; k < 4; k++) {
                    Coordinate c = nds_ij.get(i + ijShift[k * 2]).get(j + ijShift[k * 2 + 1]);
                    points[k * 2] = c.x;
                    points[k * 2 + 1] = c.y;
                }
                Quadrangle quadrangle = new Quadrangle(points);
                quads.add(new QuadratureDomains.Quad(quadrangle));
            }
        }
    }

    public double[] getStress(Coordinate pt, double[] result) {
        double theta = Math.atan2(pt.y, pt.x);
        double r=Math.sqrt(pt.x*pt.x+pt.y*pt.y);
        double a_r=a/r;
        double a_r_s=a_r*a_r;
        double a_r_q=a_r_s*a_r_s;
        double sigma_x=S*(1-(1.5*Math.cos(2*theta)+Math.cos(4*theta))*a_r_s+1.5*Math.cos(4*theta)*a_r_q);
        double sigma_y=S*(-(0.5*Math.cos(theta*2)-Math.cos(4*theta))*a_r_s-1.5*Math.cos(4*theta)*a_r_q);
        double tau_xy=S*(-(0.5*Math.sin(2*theta)+Math.sin(4*theta))*a_r_s+1.5*Math.sin(4*theta)*a_r_q);
        if(result==null){
            result=new double[]{sigma_x,sigma_y,tau_xy};
        }else{
            result[0]=sigma_x;
            result[1]=sigma_y;
            result[2]=tau_xy;
        }
                
        return result;
    }

    private void genBoundries() {
        bnds = new ArrayList<>((n_c - 1) + (n_t - 1) * 2 + (n_phi - 1) * 2);
        d_r_u_l_c_bnds = new ArrayList<>(5);
        //down:
        d_r_u_l_c_bnds.add(new ArrayList<LineBoundary>(n_t - 1));
        for (int j = 0; j < n_t - 1; j++) {
            LineBoundary t = new LineBoundary(nds_ij.get(0).get(j), nds_ij.get(0).get(j + 1));
            bnds.add(t);
            d_r_u_l_c_bnds.get(DOWN_LINE_INDEX).add(t);
        }

        //right and up:
        d_r_u_l_c_bnds.add(new ArrayList<LineBoundary>(n_phi - 1));
        d_r_u_l_c_bnds.add(new ArrayList<LineBoundary>(n_phi - 1));
        for (int i = 0; i < n_c - 1; i++) {
            LineBoundary t = new LineBoundary(nds_ij.get(i).get(n_t - 1), nds_ij.get(i + 1).get(n_t - 1));
            bnds.add(t);
            if (i < n_phi - 1) {
                d_r_u_l_c_bnds.get(RIGHT_LINE_INDEX).add(t);
            } else {
                d_r_u_l_c_bnds.get(UP_LINE_INDEX).add(t);
            }
        }
        //left:
        d_r_u_l_c_bnds.add(new ArrayList<LineBoundary>(n_phi - 1));
        for (int j = n_t - 1; j > 0; j--) {
            LineBoundary t = new LineBoundary(nds_ij.get(n_c - 1).get(j), nds_ij.get(n_c - 1).get(j - 1));
            bnds.add(t);
            d_r_u_l_c_bnds.get(LEFT_LINE_INDEX).add(t);
        }
        //circle_curve:
        d_r_u_l_c_bnds.add(new ArrayList<LineBoundary>(n_c - 1));
        for (int i = n_c - 1; i > 0; i--) {
            LineBoundary t = new LineBoundary(nds_ij.get(i).get(0), nds_ij.get(i - 1).get(0));
            bnds.add(t);
            d_r_u_l_c_bnds.get(CIRCLE_INDEX).add(t);
        }
    }

    public BoundaryCondition getNeumannBC() {
        return new BoundaryCondition() {
            boolean right;

            @Override
            public boolean setBoundary(Boundary bnd) {
                if (bnd.getNode(0).y == d && bnd.getNode(1).y == d) {
                    right = false;
                } else if (bnd.getNode(0).x == d && bnd.getNode(1).x == d) {
                    right = true;
                } else {
                    throw new IllegalArgumentException("The input boundary must be at right side or up side of this model");
                }
                return true;
            }

            @Override
            public void values(Coordinate input, double[] results, boolean[] validities) {
                double[] stress=getStress(input, null);
                if(right){
                    results[0]=stress[0];
                    results[1]=stress[2];
                }else{
                    results[0]=stress[2];
                    results[1]=stress[1];
                }
                validities[0]=true;
                validities[1]=true;
            }
        };
    }
    
    public BoundaryCondition getDirichletBC(){
        return new BoundaryCondition() {
            boolean left;
            @Override
            public boolean setBoundary(Boundary bnd) {
                if (bnd.getNode(0).y == 0 && bnd.getNode(1).y == 0) {
                    left = false;
                } else if (bnd.getNode(0).x == 0 && bnd.getNode(1).x == 0) {
                    left = true;
                } else {
                    throw new IllegalArgumentException("The input boundary must be at down side or left side of this model");
                }
                return true;
            }

            @Override
            public void values(Coordinate input, double[] results, boolean[] validities) {
                if(left){
                    validities[0]=true;
                    validities[1]=false;
                }else{
                    validities[0]=false;
                    validities[1]=true;
                }
                results[0]=0;
                results[1]=0;
            }
        };
    }

    public WeakformProblem getWeakformProblem(final int power) {
        return new WeakformProblem() {
            @Override
            public QuadraturePointIterator volumeIterator() {
                return QuadraturePointIterators.fromDomains(quads, power);
            }

            @Override
            public QuadraturePointIterator neumannIterator() {
                LinkedList<LineBoundary> neuBnds=new LinkedList<>();
                neuBnds.addAll(d_r_u_l_c_bnds.get(RIGHT_LINE_INDEX));
                neuBnds.addAll(d_r_u_l_c_bnds.get(UP_LINE_INDEX));
                return QuadraturePointIterators.fromLineBoundariesAndBC(power,neuBnds, getNeumannBC());
            }

            @Override
            public QuadraturePointIterator dirichletIterator() {
                LinkedList<LineBoundary> diriBnds=new LinkedList<>();
                diriBnds.addAll(d_r_u_l_c_bnds.get(DOWN_LINE_INDEX));
                diriBnds.addAll(d_r_u_l_c_bnds.get(LEFT_LINE_INDEX));
                return QuadraturePointIterators.fromLineBoundariesAndBC(power,diriBnds, getDirichletBC());
            }

            @Override
            public List<Node> dirichletNodes() {
                ArrayList<Node> result=new ArrayList<>(n_t*2);
                int[] indes=new int[]{0,n_c-1};
                for(int i=0;i<indes.length;i++){
                    int i_index=indes[i];
                    for(int j=0;j<n_t;j++){
                        result.add(nds_ij.get(i_index).get(j));
                    }
                }
                return result;
            }

            @Override
            public VolumeCondition volumeCondition() {
                return null;
            }
        };
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model.mechanics;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;
import net.epsilony.simpmeshfree.model.geometry.Node;

/**
 *
 * @author epsilon
 */
public class GeneticBandWidthReducer {

    int mutationSwap;
    int[][] nodeNeighbors;
    int[] flags;
    Chromosome[] population;
    int populationSize;
    int populationCapacity;
    int length;
    double mutationRate;
//    Node[] nodes;
    public static final Comparator<Chromosome> chromoComp = new Comparator<Chromosome>() {

        @Override
        public int compare(Chromosome o1, Chromosome o2) {
            return o1.bandWidth - o2.bandWidth;
        }
    };

    public Chromosome[] getPopulation() {
        return population;
    }

    public class Chromosome {

        public boolean isSame(Chromosome chrm) {
            if (bandWidth != chrm.bandWidth) {
                return false;
            }
            if (criticalNodes.size() != chrm.criticalNodes.size()) {
                return false;
            }
            for (int i = 0; i < matrixIndes.length; i++) {
                if (chrm.matrixIndes[i] != matrixIndes[i]) {
                    return false;
                }
            }
            return true;
        }
        int[] matrixIndes;
        int[] bandWidths;
        int bandWidth;
        int[] nodeMids;
        TreeSet<Integer> criticalNodes = new TreeSet<Integer>();

        public Chromosome() {
            matrixIndes = new int[length];
            bandWidths = new int[length];
            nodeMids = new int[length];
        }

        public int bandWidth() {
            if (criticalNodes.size() > 0) {
                return bandWidth;
            }
            int i, size;
            for (i = 0; i < length; i++) {
                size = bandWidthOfNode(i);

                if (size > bandWidth) {
                    bandWidth = size;
                }
            }
            for (i = 0; i < length; i++) {
                if (bandWidths[i] == bandWidth) {
                    criticalNodes.add(i);
                }
            }
            return bandWidth;
        }

        public int bandWidthOfNode(int i) {
            int size = 0;
            int[] neighbors = nodeNeighbors[i];
            int min, max, neiborI;
            min = max = neighbors[0];
            for (int j = 0; j < neighbors.length; j++) {
                neiborI = matrixIndes[neighbors[j]];
                if (neiborI < min) {
                    min = neiborI;
                }
                if (neiborI > max) {
                    max = neiborI;
                }
                if (Math.abs(neiborI - matrixIndes[i]) > size) {
                    size = Math.abs(neiborI - matrixIndes[i]);
                }
            }
            bandWidths[i] = size;
            nodeMids[i] = (min + max) / 2;
            return size;
        }

        public int bandWidthOfNode(int i, int asumption) {
            int size = 0;
            int[] neighbors = nodeNeighbors[i];
            for (int j = 0; j < neighbors.length; j++) {
                if (Math.abs(matrixIndes[neighbors[j]] - matrixIndes[asumption]) > size) {
                    size = Math.abs(matrixIndes[neighbors[j]] - matrixIndes[asumption]);
                }
            }
            return size;
        }

        public int criticalValueOfNode(int i) {
            int size = bandWidthOfNode(i);
            if (size < bandWidth) {
                return 0;
            } else {
                if (size == bandWidth) {
                    return 1;
                } else {
                    return 2;
                }
            }
        }

        public int criticalValueOfNode(int i, int assumption) {
            int size = bandWidthOfNode(i, assumption);
            if (size < bandWidth) {
                return 0;
            } else {
                if (size == bandWidth) {
                    return 1;
                } else {
                    return 2;
                }
            }
        }

        public int hillClimbing() {
            boolean canImprove;
            int[] neighbors;
            int cv2, cu2, cu;
            int j, k, u, v, t;
            bandWidth();
            do {
                canImprove = false;
                for (Integer i : criticalNodes) {
                    v = i;
                    neighbors = nodeNeighbors[v];
                    for (j = 0; j < neighbors.length; j++) {
                        u = neighbors[j];
                        if (Math.abs(nodeMids[v] - matrixIndes[u]) >= Math.abs(nodeMids[v] - matrixIndes[v])) {
                            continue;
                        }
                        cu = bandWidths[u];
                        cu2 = bandWidthOfNode(u, v);
                        cv2 = bandWidthOfNode(v, u);
                        if (cu2 <= cu && cv2 <= bandWidth && (cu2 + cv2) <= (cu + bandWidth)) {
                            t = matrixIndes[v];
                            matrixIndes[v] = matrixIndes[u];
                            matrixIndes[u] = t;

                            if (0 == cv2) {
                                criticalNodes.remove(v);
                            }
                            if (cu2 < cu) {
                                criticalNodes.remove(u);
                            }
                            for (k = 0; k < neighbors.length; k++) {
                                if (bandWidthOfNode(neighbors[k]) == bandWidth) {
                                    criticalNodes.add(neighbors[k]);
                                }
                            }
                            neighbors = nodeNeighbors[v];
                            for (k = 0; k < neighbors.length; k++) {
                                if (bandWidthOfNode(neighbors[k]) == bandWidth) {
                                    criticalNodes.add(neighbors[k]);
                                }
                            }
                            canImprove = true;
                            break;
                        }
                    }
                }
            } while (canImprove);
            return bandWidth();
        }

        Chromosome bfsChromosome() {
            return bfsChromosome(new Random().nextInt(flags.length));
        }

        Chromosome bfsChromosome(int start) {

            matrixIndes[0] = start;

            //breath first search
            int f = ++flags[0];
            int[] neighbors;
            int end = 1;
            for (int i = 0; i < matrixIndes.length && end < matrixIndes.length; i++) {
                neighbors = nodeNeighbors[matrixIndes[i]];
                for (int j = 0; j < neighbors.length; j++) {
                    if (flags[neighbors[j]] != f) {
                        flags[neighbors[j]] = f;
                        matrixIndes[end] = neighbors[j];
                        end++;
                    }
                }
            }
            return this;
        }
    }

    public GeneticBandWidthReducer(List<Node> nodes, int populationCapacity, double crossOverRate, double mutationRate, int mutationSwap) {
        int i = 0, j;

        this.length = nodes.size();
        for (Node n : nodes) {
            n.flag = i;
            i++;
        }
        i = 0;
        int[] indes;
        for (Node n : nodes) {
            TreeSet<Node> neiSet = n.getNeighbors();
            indes = new int[neiSet.size()];
            nodeNeighbors[i] = indes;
            j = 0;
            for (Node nn : neiSet) {
                indes[j] = nn.flag;
                j++;
            }
            i++;
        }

        this.mutationRate = mutationRate;
        this.populationCapacity = populationCapacity;
        population = new Chromosome[populationCapacity + (int) Math.ceil(populationCapacity * crossOverRate) * 2];
        for (i = 0; i < populationCapacity; i++) {
            population[i] = new Chromosome();
            population[i].bfsChromosome();
        }
        this.populationSize = this.populationCapacity;
        for (i = this.populationCapacity; i < population.length; i++) {
            population[i] = new Chromosome();
        }
        this.mutationSwap = mutationSwap;
    }

    void crossOver() {
        populationSize = populationCapacity;
        Random r;
        while (populationSize < population.length) {
            r = new Random();
            crossOver(r.nextInt(length), r.nextInt(length));
        }
    }

    void crossOver(int motherI, int fatherI) {
        Chromosome mother, father, son, daughter;
        mother = population[motherI];
        father = population[fatherI];
        son = population[populationSize];
        daughter = population[populationSize + 1];
        populationSize += 2;
        int f = flags[0] + 1;
        int i, j;
        for (i = 0; i < length / 2; i++) {
            son.matrixIndes[i] = father.matrixIndes[i];
            flags[father.matrixIndes[i]] = f;
        }
        for (i = 0, j = length / 2; i < length && j < length; i++) {
            if (flags[mother.matrixIndes[i]] != f) {
                son.matrixIndes[j] = mother.matrixIndes[i];
                flags[mother.matrixIndes[i]] = f;
                j++;
            }
        }

        f = flags[0] + 1;
        for (i = 0; i < length / 2; i++) {
            daughter.matrixIndes[length - i - 1] = mother.matrixIndes[length - i - 1];
            flags[mother.matrixIndes[i]] = f;
        }
        for (i = 0, j = 0; i < length && j < length / 2; i++) {
            if (flags[father.matrixIndes[i]] != f) {
                daughter.matrixIndes[j] = father.matrixIndes[i];
                flags[father.matrixIndes[i]] = f;
            }
        }
    }

    void mutate() {
        int i, j, u, v, t;
        Chromosome chrm;
        Random r;
        for (i = populationCapacity; i < population.length; i++) {
            chrm = population[i];
            r = new Random();
            if (r.nextDouble() <= mutationRate) {
                for (j = 0; j < Math.ceil(mutationSwap * r.nextDouble()); j++) {
                    r = new Random();
                    u = r.nextInt(length);
                    v = r.nextInt(length);
                    t = chrm.matrixIndes[v];
                    chrm.matrixIndes[v] = chrm.matrixIndes[u];
                    chrm.matrixIndes[u] = t;
                }
            }
        }
    }

    void mergeTheSame() {
        int front = 0;
        for (int i = 1; i < populationCapacity; i++) {
            if (!population[i].isSame(population[front])) {
                front++;
                if (front != i) {
                    population[front] = population[i];
                }
            }
        }
    }

    public void generate() {
        crossOver();
        mutate();
        for (int i = populationCapacity; i < population.length; i++) {
            population[i].hillClimbing();
        }
        Arrays.sort(population, chromoComp);
        mergeTheSame();
    }
}

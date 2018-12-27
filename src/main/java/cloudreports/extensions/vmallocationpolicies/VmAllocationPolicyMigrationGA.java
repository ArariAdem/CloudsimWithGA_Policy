package cloudreports.extensions.vmallocationpolicies;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.util.ExecutionTimeMeasurer;

public class VmAllocationPolicyMigrationGA extends
        VmAllocationPolicyMigrationAbstract {

    public static Random rnd;

    List<GAInd> pop, pareto;

    public VmAllocationPolicyMigrationGA(List<? extends Host> hostList,
            VmSelectionPolicy vmSelectionPolicy) {
        super(hostList, vmSelectionPolicy);
        // TODO Auto-generated constructor stub
        Log.setDisabled(true);
    }

    @Override
    public List<Map<String, Object>> optimizeAllocation(
            List<? extends Vm> vmList) {
        ExecutionTimeMeasurer.start("optimizeAllocationTotal");

        List<Map<String, Object>> migrationMap = new LinkedList<Map<String, Object>>();
        // populate migrationMap here

        ExecutionTimeMeasurer.start("optimizeAllocationHostSelection");
        initGA();
        getExecutionTimeHistoryHostSelection().add(
                ExecutionTimeMeasurer.end("optimizeAllocationHostSelection"));
        while (true) {
            try {
                migrationMap = pareto.get(rnd.nextInt(pareto.size())).getMap();
                break;
            } catch (Exception e) {
            }
        }

        getExecutionTimeHistoryTotal().add(
                ExecutionTimeMeasurer.end("optimizeAllocationTotal"));
        return migrationMap;
    }

    @Override
    protected boolean isHostOverUtilized(PowerHost host) {
        // TODO Auto-generated method stub
        return false;
    }

    private void initGA() {
        rnd = new Random();
        pop = new ArrayList<GAInd>();
        pareto = new ArrayList<GAInd>();

        for (int i = 0; i < 50; i++) {
            try {
                addToPopulation(new GAInd(this));
            } catch (Exception e) {
                // TODO Auto-generated catch block
                System.out.println(e.getMessage());
            }
        }

        for (int i = 0; i < 500; i++) {
            mutation();
            crossover();
        }
    }

    private void crossover() {
        GAInd p1, p2;
        p1 = pop.get(rnd.nextInt(pop.size()));
        do {
            p2 = pop.get(rnd.nextInt(pop.size()));
        } while (p1.equals(p2));

        try {
            addToPopulation(new GAInd(p1, p2));
        } catch (Exception e) {
        }
        //crossover takes too much time
        /*for (int i = 0; i < pop.size(); i++) {
			if (rnd.nextInt(100) < 90) {
				GAInd p1, p2;
				p1 = pop.get(rnd.nextInt(pop.size()));
				do {
					p2 = pop.get(rnd.nextInt(pop.size()));
				} while (p1.equals(p2));
				
				try {
					addToPopulation(new GAInd(p1, p2));
				} catch (Exception e) {
				}
			}
		}*/
    }

    private void mutation() {
        for (GAInd ind : pop) {
            if (!ind.isPareto && rnd.nextInt(1000) < 10) {
                try {
                    ind.Mutation();
                } catch (Exception e) {
                }
            }
        }
    }

    private void removeIndividual(GAInd ind) {
        pop.remove(ind);
        pareto.remove(ind);
    }

    private boolean addToPareto(GAInd ind) {
        List<GAInd> dominatedInds = new ArrayList<GAInd>();
        for (GAInd target : pareto) {
            if (ind.dominates(target) == Domination.True) {
                dominatedInds.add(target);
            } else if (ind.dominates(target) == Domination.False) {
                return false;
            }
        }

        for (GAInd gaInd : dominatedInds) {
            removeIndividual(gaInd);
        }
        if (pareto.size() < 20) {
            pareto.add(ind);
            ind.isPareto = true;
            return true;
        }
        return false;
    }

    private boolean addToPopulation(GAInd ind) {
        if (pop.size() < 50) {
            pop.add(ind);
            addToPareto(ind);
            return true;
        }
        List<GAInd> dominatedInds = new ArrayList<GAInd>();
        for (GAInd gaInd : pop) {
            if (ind.dominates(gaInd) == Domination.True) {
                dominatedInds.add(gaInd);
            }
        }

        if (dominatedInds.size() > 0) {
            GAInd random = dominatedInds.get(rnd.nextInt(dominatedInds.size()));
            removeIndividual(random);
            pop.add(ind);
            addToPareto(ind);
            return true;
        }
        return false;
    }

}

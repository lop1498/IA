package src;

import aima.search.framework.Successor;
import aima.search.framework.SuccessorFunction;
import aima.util.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static src.Main.n;

/**
 * This mapSuccesor function tries to minimize the distance as well as the number of drivers
 **/

public class MapSuccesors2 implements SuccessorFunction {

    public List getSuccessors(Object state) {
        ArrayList retVal = new ArrayList();  //we must add all posibilities from a current state to this list
        Map map = (Map) state;
        ArrayList<Boolean> estaRecullit = map.getEstaRecullit();

        //SWAP DRIVERS
        for (int i = 0; i < map.getEstatConductors().size(); ++i) {
            for (int j = i + 1; j < map.getEstatConductors().size(); ++j) {
                Map aux = new Map(map); // copy of map
                aux.changeDrivers(i, j);
                retVal.add(new Successor("Canviem els passatgers dels cotxes " + i + " i el " + j, aux));
            }
        }

        //SEND TO OTHER CAR (REMOVE+ADD)
        for (int i = 0; i < map.getEstatConductors().size(); ++i) {
            HashSet<Integer> p = map.getPassangersNotRepeated(i);
            for (Integer a : p) {
                for (int j = 0; j < map.getEstatConductors().size(); ++j) {
                    Map aux = new Map(map); // copy of map
                    aux.rmPerson(a, i);
                    aux.addPerson(a, j);
                    retVal.add(new Successor("Canviem la persona " + a + " del cotxe " + i + " al cotxe " + j, aux));
                }
            }
        }

        //DELETE DRIVERS
        for (int i = 0; i < map.getEstatConductors().size(); ++i)  //iterate over all drivers
        {
            if (map.getPassangers(i).size() == 0) {
                Map aux = new Map(map);
                if (aux.removeDriver(i)) {
                    int index_persona = (Integer)((Pair)map.getEstatConductors().get(i).getFirst()).getSecond();
                    retVal.add(new Successor("Hem eliminat el cotxe "+i+" conduit per la persona "+index_persona, aux));
                }
            }
        }


        //ADD PERSON
        for (int i = 0; i < estaRecullit.size(); ++i) {
            //we need to check that this is really not a driver. Not that it could be one
            if (!estaRecullit.get(i)) {
                for (int c = 0; c < map.getEstatConductors().size(); ++c) {
                    Map aux = new Map(map); // copy of map
                    aux.setEstaRecullit(i, true);
                    aux.addPerson(i, c);
                    retVal.add(new Successor("Afegim una persona al cotxe " + c, aux));
                }
            }
        }


        ArrayList<HashSet<Integer>> uniquePassengers = passangersFromAllCars(map);


        //REMOVE PERSON
        for (int c = 0; c < map.getEstatConductors().size(); ++c)   //we iterate over all the drivers
        {
            HashSet<Integer> p = uniquePassengers.get(c);

            for (int k : p) {
                Map aux = new Map(map);
                aux.rmPerson(k, c);
                retVal.add(new Successor("Borrem la persona " + k + " del cotxe" + c, aux));   //the driver drives alone
            }
        }


        //SWAP CAR
        boolean[][] hanFetSwap = new boolean[n][n];

        for (int i = 0; i < map.getEstatConductors().size(); ++i) {
            HashSet<Integer> p1 = uniquePassengers.get(i);
            for (int j = i + 1; j < map.getEstatConductors().size(); ++j) {
                HashSet<Integer> p2 = uniquePassengers.get(j);
                for (int k : p1)    //person k of the first car
                {
                    for (int l : p2) //person l of the second car
                    {
                        //if the swap of these two people has not yet been done
                        if (!hanFetSwap[i][j] && !hanFetSwap[i][j]) {
                            hanFetSwap[i][j] = true;
                            hanFetSwap[j][j] = true;
                            Map aux = new Map(map);
                            aux.swapCar(k, l, i, j);
                            retVal.add(new Successor("Fem swap de les persones " + k + " " + l + " dels cotxes " + i + " i " + j, aux));
                        }
                    }
                }
            }
        }


        //FALTARA EL SWAP ORDER
        for (int c = 0; c < map.getEstatConductors().size(); ++c) {
            HashSet<Integer> p1 = uniquePassengers.get(c);
            for (int j = 0; j < p1.size() * 2; ++j) {
                for (int k = j + 1; k < p1.size() * 2; ++k) {
                    Map aux = new Map(map); // copy of map
                    if (aux.swapOrder(j, k, c))   //we enter only if the operation is not between the same person
                    {
                        retVal.add(new Successor("Fem swap order del cotxe de la pe" + c + " dels passatgers " + j + " i " + k, aux));
                    }
                }
            }
        }

        return retVal;
    }


    private ArrayList<HashSet<Integer>> passangersFromAllCars(Map currentState) {
        ArrayList<HashSet<Integer>> a = new ArrayList<>();
        for (int c = 0; c < currentState.getEstatConductors().size(); ++c)
            a.add(currentState.getPassangersNotRepeated(c));

        return a;
    }


}

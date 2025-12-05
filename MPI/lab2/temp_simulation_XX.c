#include <mpi.h>
#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <float.h>

const double L = 100.0;                 // Length of the 1d domain
const int n = 1000;                     // Total number of points
const int iterations_per_round = 1000;  // Number of iterations for each round of simulation
const double allowed_diff = 0.001;      // Stopping condition: maximum allowed difference between values


double initial_condition(double x, double L) {
    return fabs(x - L / 2);
}


int main(int argc, char **argv) {
    MPI_Init(&argc, &argv);


    int rank, size;
    MPI_Comm_rank(MPI_COMM_WORLD, &rank);
    MPI_Comm_size(MPI_COMM_WORLD, &size);


    // TODO
    // Variables declaration and initialization
    
    double local_size = n/size;
    double *temperatures = (double *) malloc(sizeof(double) * local_size);
    double prev_temp;
    double succ_temp;

    // TODO
    // Set initial conditions
    
    double l_over_size = L/size;
    for(int i = 0; i < local_size; i++){
      temperatures[i] = initial_condition(l_over_size*rank + (i/n)*L, L);
    }
    
    double min_temp;
    double max_temp;
    MPI_Request request;

    int round = 0;
    while (1) {
        // Perform one round of iterations
        round++;
        for (int t = 0; t < iterations_per_round; t++) {
            // TODO
            // Implement the code for each iteration
	    
	    max_temp = DBL_MIN;
	    min_temp = DBL_MAX;
	    
            for (int i = 0; i < local_size; i++){
	        if (rank == 0 && i == 0){
                    prev_temp = temperatures[i];
		    temperatures[i] = (temperatures[i] + temperatures[i+1]) / 2;
		} else if (rank == size - 1 && i == local_size - 1){
		    temperatures[i] = (prev_temp + temperatures[i]) / 2;
		} else if (i == 0){
		    MPI_Isend(&temperatures[i], 1, MPI_DOUBLE, rank-1, 0, MPI_COMM_WORLD, &request);
	            MPI_Recv(&succ_temp, 1, MPI_DOUBLE, rank-1, MPI_ANY_TAG, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
		    prev_temp = temperatures[i];
		    temperatures[i] = (succ_temp + temperatures[i] + temperatures[i+1]) / 3;
	        } else if (i == local_size - 1){
		    MPI_Isend(&temperatures[i], 1, MPI_DOUBLE, rank+1, 0, MPI_COMM_WORLD, &request);
		    MPI_Recv(&succ_temp, 1, MPI_DOUBLE, rank+1, MPI_ANY_TAG, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
		    temperatures[i] = (prev_temp + temperatures[i] + succ_temp) /3;
		} else {
		    succ_temp = temperatures[i];
		    temperatures[i] = (prev_temp + temperatures[i] + temperatures[i+1])/3;
		    prev_temp = succ_temp;
		}
		
		if (temperatures[i] > max_temp) max_temp = temperatures[i];
		if (temperatures[i] < min_temp) min_temp = temperatures[i];

            }
        }


        // TODO
        // Compute global minimum and maximum
        double global_min, global_max, max_diff;

	MPI_Allreduce(&max_temp, &global_max, 1, MPI_DOUBLE, MPI_MAX, MPI_COMM_WORLD);
	MPI_Allreduce(&min_temp, &global_min, 1, MPI_DOUBLE, MPI_MIN, MPI_COMM_WORLD);
	max_diff = global_max - global_min;
        
        if (rank == 0) {
            printf("Round: %d\tMin: %f.5\tMax: %f.5\tDiff: %f.5\n", round, global_min, global_max, max_diff);
        }
        
        // TODO
        // Implement stopping conditions (break)
	
	if (max_diff < allowed_diff) break;	

    }


    // TODO 
    // Deallocation

    free(temperatures);

    MPI_Finalize();
    return 0;
}


#include <mpi.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>

// Set DEBUG 1 if you want car movement to be deterministic
#define DEBUG 0

const int num_segments = 256;

const int num_iterations = 1000;
const int count_every = 10;

const double alpha = 0.5;
const int max_in_per_sec = 10;

// Returns the number of car that enter the first segment at a given iteration.
int create_random_input() {
#if DEBUG
  return 1;
#else
  return rand() % max_in_per_sec;
#endif
}

// Returns 1 if a car needs to move to the next segment at a given iteration, 0 otherwise.
int move_next_segment() {
#if DEBUG
  return 1;
#else
  return rand() < alpha ? 1 : 0;
#endif
}

int main(int argc, char** argv) { 
  MPI_Init(NULL, NULL);

  int rank;
  int num_procs;
  MPI_Comm_rank(MPI_COMM_WORLD, &rank);
  MPI_Comm_size(MPI_COMM_WORLD, &num_procs);
  srand(time(NULL) + rank);
  
  // TODO: define and init variables

  int num_segments_per_proc = num_segments/num_procs;
  int *local_road = (int *) malloc(sizeof(int) * num_segments_per_proc);
  memset( (void *) local_road, 0, sizeof(int) * num_segments_per_proc);

  // Simulate for num_iterations iterations
  for (int i = 0; i < num_iterations; ++i) {
    // Move cars across segments
    // New cars may enter in the first segment
    // Cars may exit from the last segment

    int car_exiting = 0;
    for (int j = 0; j < local_road[num_segments_per_proc - 1]; j++){
	   if (move_next_segment){
		  car_exiting++;
	   }
    }
    local_road[num_segments_per_proc - 1] -= car_exiting;
    if (rank < num_procs - 1) {
	   MPI_Send(&car_exiting, 1, MPI_INT, rank + 1, 0, MPI_COMM_WORLD);
    }

    for (int j = num_segments_per_proc - 2; j >= 0; j--){
    	   int cars_to_move = 0;
	   for (int k = 0; k < local_road[k]; k++){
		if (move_next_segment()){
		     cars_to_move++;
		}
           }
	   local_road[j] -= cars_to_move;
           local_road[j + 1] += cars_to_move;	   
    } 

    if (rank == 0){
	    local_road[0] += create_random_input();
    } else {
	    int entered = 0;
	    MPI_Recv(&entered, 1, MPI_INT, rank - 1, 0, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
	    local_road[0] += entered;
    }

    // When needed, compute the overall sum
    if (i%count_every == 0) {
      int global_sum = 0;

      // TODO compute global sum
      
      int local_sum = 0;
      for (int j = 0; j < num_segments_per_proc; j++){
              local_sum += local_road[j];
      }

      MPI_Reduce(&local_sum, &global_sum, 1, MPI_INT, MPI_SUM, 0, MPI_COMM_WORLD); 
      
      if (rank == 0) {
	printf("Iteration: %d, sum: %d\n", i, global_sum);
      }
    }
    
    MPI_Barrier(MPI_COMM_WORLD);
  }

  // TODO deallocate dynamic variables, if needed
  
  free(local_road);
  
  MPI_Finalize();
}

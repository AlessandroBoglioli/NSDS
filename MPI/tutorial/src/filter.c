#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <mpi.h>

// Creates an array of random numbers.
int *create_random_array(int num_elements, int max_value) {
  int *arr = (int *) malloc(sizeof(int)*num_elements);
  for (int i=0; i<num_elements; i++) {
    arr[i] = (rand() % max_value);
  }
  return arr;
}

int get_num_multiples(int *in, int size, int num) {
  int count = 0;
  for (int i=0; i<size; i++) {
    if ((in[i] % num) == 0) {
      count++;
    }
  }
  return count;
}

int * get_multiples(int *in, int size, int num, int num_multiples) {
  int *result = (int *) malloc(sizeof(int) * num_multiples);
  int j = 0;
  for (int i=0; i<size; i++) {
    if ((in[i] % num) == 0) {
      result[j++] = in[i];
    }
  }
  return result;
}

void send_filtered_results_to_master(int my_rank, int *in, int size, int num) {
  int num_multiples = get_num_multiples(in, size, num);
  
  MPI_Send(
	&num_multiples, 
	1, 
	MPI_INT, 
	0, 
	0, 
	MPI_COMM_WORLD);

  printf("Node %d sends %d results to the master\n", my_rank, num_multiples); 
  if (num_multiples > 0) {
    int *out = get_multiples(in, size, num, num_multiples);
    MPI_Send(out, num_multiples, MPI_INT, 0, 0, MPI_COMM_WORLD);

    free(out);
  }
}

int * master_receive_results(int world_size, int *out_size) {

  int num_receive = 0;
  *out_size = 0;

  for (int proc=1; proc<world_size; proc++) {

    int proc_num_results;
    
    MPI_Recv(
	&proc_num_results, 
	1, 
	MPI_INT, 
	proc, 
	MPI_ANY_TAG, 
	MPI_COMM_WORLD, 
	MPI_STATUS_IGNORE);

    (*out_size) += proc_num_results;
    if (proc_num_results > 0) {
      num_receive++;
    }
  }

  int *result = NULL;
  if (*out_size == 0) return result;
  result = (int *) malloc(sizeof(int) * (*out_size));
  printf("Master ready to receive %d results\n", *out_size);
  
  int first_free_id = 0;
  for (int i=0; i<num_receive; i++) {
    MPI_Status status;
    MPI_Probe(MPI_ANY_SOURCE, MPI_ANY_TAG, MPI_COMM_WORLD, &status);
    int count;
    MPI_Get_count(&status, MPI_INT, &count);
    MPI_Recv(&result[first_free_id], count, MPI_INT, status.MPI_SOURCE, status.MPI_TAG, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
    first_free_id += count;
  }

  return result;
}

// Process 0 selects a number num.
// All other processes have an array that they filter to only keep the elements
// that are multiples of num.
// Process 0 collects the filtered arrays and print them.

int main(int argc, char** argv) {

  // Maximum value for each element in the arrays
  const int max_val = 100;

  // Number of elements for each processor
  int num_elements_per_proc = 50;
  
  // Number to filter by
  int num_to_filter_by = 2;
  
  if (argc > 1) {
    num_elements_per_proc = atoi(argv[1]);
  }

  // Init random number generator
  srand(time(NULL));

  MPI_Init(NULL, NULL);

  int my_rank, world_size; 
  MPI_Comm_rank(MPI_COMM_WORLD, &my_rank);
  MPI_Comm_size(MPI_COMM_WORLD, &world_size);

  // Process 0 selects the num
  int num;
  int *local_array;
  if (my_rank == 0) {
    num = num_to_filter_by;
  } else {
    local_array = create_random_array(num_elements_per_proc, max_val);
  }

  // TODO
  
  MPI_Bcast(
	&num,
	1,
	MPI_INT,
	0,
	MPI_COMM_WORLD);

  if (my_rank != 0){
    send_filtered_results_to_master(my_rank, local_array, num_elements_per_proc, num);
  } else {
    int num_results;
    int *results = master_receive_results(world_size, &num_results);
    printf("Recived %d results\n", num_results);
    for (int i = 0; i < num_results; i++){
      printf("%d\t", results[i]);
    }
    if (num_results > 0) {
      printf("\n");
      free(results);
    }
  }
  
  MPI_Barrier(MPI_COMM_WORLD);
  MPI_Finalize();
}

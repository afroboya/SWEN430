#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <assert.h>

/**
 * Compare two compound objects for equality.
 */
int64_t intcmp(const int64_t* obj1, const int64_t *obj2) {
  int64_t obj1_len = *obj1;
  int64_t obj2_len = *obj2;
  // Check same lengths
  if(obj1_len != obj2_len) {
    return 0; // false
  } else {
    // Compare each element in turn
    int j = 1;
    for(int i=0;i<obj1_len;i=i+1) {
      // Check element
      if(obj1[j] != obj2[j]) {	
	return 0; // false
      } 
      // Skip payload
      j = j + 1;
    }
    // Done
    return 1; // true
  }    
}

/**
 * Shallow clone a given object.
 */
int64_t *intcpy(int64_t *obj) {
  // Determine object length
  int64_t len = obj[0];
  // Determine width (in words)
  int64_t n = 1+len;
  // Allocate sufficient space
  int64_t *cpy = malloc(sizeof(int64_t) * n);
  // bitwise copy elements
  for(int i=0;i<n;++i) {
    cpy[i] = obj[i];
  }
  // done
  return cpy;
}

/**
 * Fill every element of object with tag and payload.
 */
void intfill(int64_t *obj, int64_t payload) {
  // Determine object length
  int64_t len = obj[0];
  // advance over length field
  for(int i=0,j=1;i<len;++i) {
    obj[j++] = payload;
  }
}


/**
 * Compare two compound objects for equality.
 */
int64_t objcmp(const int64_t* obj1, const int64_t *obj2) {
  int64_t obj1_len = *obj1;
  int64_t obj2_len = *obj2;
  // Check same lengths
  if(obj1_len != obj2_len) {
    return 0; // false
  } else {
    // Compare each element in turn
    int j = 1;
    for(int i=0;i<obj1_len;i=i+1) {
      // Extract tag
      int64_t tag1 = obj1[j];
      int64_t tag2 = obj2[j++];
      //
      if(obj1[j] == obj2[j]) {
	// All good
      } else if(tag1 != tag2 || tag1 == 0) {
	return 0; // false
      } else if(objcmp((int64_t*)obj1[j],(int64_t*)obj2[j]) == 0) {
	return 0; // false
      }
      // Skip payload
      j = j + 1;
    }
    // Done
    return 1; // true
  }    
}

/**
 * Shallow clone a given object.
 */
int64_t *objcpy(int64_t *obj) {
  // Determine object length
  int64_t len = obj[0];
  // Determine width (in words)
  int64_t n = 1+(len*2);
  // Allocate sufficient space
  int64_t *cpy = malloc(sizeof(int64_t) * n);
  // bitwise copy elements
  for(int i=0;i<n;++i) {
    cpy[i] = obj[i];
  }
  // done
  return cpy;
}

/**
 * Fill every element of object with tag and payload.
 */
void objfill(int64_t *obj, int64_t tag, int64_t payload) {
  // Determine object length
  int64_t len = obj[0];
  // advance over length field
  for(int i=0,j=1;i<len;++i) {
    obj[j++] = tag;
    obj[j++] = payload;
  }
}

/**
 * Implement an assertion in WhileLang as a C assert.  Unfortunately, 
 * we cannot call "assert" directly as a function because it is, in fact, a macro.
 */
void assertion(int64_t boolean) {
  assert(boolean);
}




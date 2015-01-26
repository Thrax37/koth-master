package controller;

import java.util.Random;

/**
 * Generate N random numbers when their SUM is known
 * 
 * @author Deepak Azad
 */

public class RandomNumberGenerator {

    public int[] genNumbers(int n, int sum){
        int[] nums = new int[n];
        int upperbound = Long.valueOf(Math.round(sum*1.0/n)).intValue();
        int offset = Long.valueOf(Math.round(0.5*upperbound)).intValue();
       
        int cursum = 0;
        Random random = new Random(new Random().nextInt());
        for(int i=0 ; i < n ; i++){
            int rand = random.nextInt(upperbound) + offset;
            if( cursum + rand > sum || i == n - 1) {
                rand = sum - cursum;
            }
            cursum += rand;
            nums[i]=rand;
            if(cursum == sum){
                break;
            }
        }
        return nums;
    }
    
    public int[] genNumberWithLimits(int sum, int[] limits) {
		
    	int n = limits.length;
    	int[] nums = new int[n];
    	int total = 0;
    	
    	for (int l : limits) {
    		total += l;
    	}
    	
    	if (total <= sum)
    		return limits;
    	
        Random random = new Random(new Random().nextInt());
    	while (sum > 0) {  
    		int x = random.nextInt(n);
    		if (nums[x] < limits[x]) {
    			nums[x] += 1;
    			sum--;
    		}
    	}
    	return nums;
    }
}
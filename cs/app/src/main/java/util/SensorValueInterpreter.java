package util;

/**
 * Created by johan on 2017-11-09.
 */

public class SensorValueInterpreter {



    public static String makeSenseOfTemperatureReadings(String data){
        float[] readings = transformToFloatArray(data);

        // calculate the mean
        float mean = 0, sum = 0;
        for (float f : readings){
            sum += f;
        }
        mean = sum / readings.length;

        return mean + "°C";
    }

    /**
     * This function interprets given light readings as following states:
     *
     * Lighting condition	From (lux)	To (lux)	Mean value (lux)
     * Pitch Black	    0	        10	        5
     * Very Dark	    11	        50	        30
     * Dark Indoors	    51	        200	        125
     * Dim Indoors	    201	        400	        300
     * Normal Indoors	401	        1000	    700
     * Bright Indoors	1001	    5000	    3000
     * Dim Outdoors	    5001	    10,000	    7500
     * Cloudy Outdoors	10,001	    30,000	    20,000
     * Direct Sunlight	30,001	    100,000	    65,000
     *
     * @param data Sensor readings
     * @return returns a sensible interpretation of the readings
     */
    public static String makeSenseOfLightReadings(String data){

        // Data = "5;10;19;33"
        // where each reading are separated with a ";"
        float[] readings = transformToFloatArray(data);

        // calculate the mean
        float mean = 0, sum = 0;
        for (float f : readings){
            sum += f;
        }
        mean = sum / readings.length;

        if(mean <= 10) {
            return "Pitch black (" + mean + " lx)";
        }
        else if(10 < mean && mean <= 50) {
            return "Very dark ("+ mean + " lx)";
        }
        else if(50 < mean && mean <= 200) {
            return "Dark indoors (" + mean + " lx)";
        }
        else if(200 < mean && mean <= 400) {
            return "Dim indoors (" + mean + " lx)";
        }
        else if(400 < mean && mean <= 1000) {
            return "Normal indoors (" + mean + " lx)";
        }
        else if(1000 < mean && mean <= 5000) {
            return "Bright indoors (" + mean + " lx)";
        }
        else if(5000 < mean && mean <= 10000) {
            return "Dim outdoors (" + mean + " lx)";
        }
        else if(10000 < mean && mean <= 30000) {
            return "Cloudy outdoors (" + mean + " lx)";
        }
        else{
            return "Direct sunlight (" + mean + " lx)";
        }

    }

    /**
     * This function interprets given pressure readings as following states:
     *
     * 1022.6894+
     * Rising or steady pressure means continued fair weather.
     * Slowly falling pressure means fair weather.
     * Rapidly falling pressure means cloudy and warmer conditions.
     *
     * 1009.1439-1022.6894
     * Rising or steady pressure means present conditions will continue.
     * Slowly falling pressure means little change in the weather.
     * Rapidly falling pressure means that rain is likely, or snow if it is cold enough.
     *
     * 1009.1439-
     * Rising or steady pressure indicates clearing and cooler weather.
     * Slowly falling pressure indicates rain
     * Rapidly falling pressure indicates a storm is coming.
     *
     * @param data the gathered pressure readings
     * @return Returns a sensible interpretation of the supplied data
     */
    public static String makeSenseOfPressureReadings(String data, String dur){

        float[] readings = transformToFloatArray(data);
        int duration = Integer.parseInt(dur);

        if (readings[0] > 1022.6894){
            if(isPressureIncreasingRapidly(readings, duration) >= 0){ // rising or steady
                return "The weather will probably continue as is.";
            }
            else if(isPressureIncreasingRapidly(readings, duration) == -1){ // slowly falling
                return "The weather will probably continue as is.";
            }
            else { // rapidly falling
                return "The weather is probably getting cloudy and warmer.";
            }
        }
        else if (readings[0] > 1009.1439 && readings[0] < 1022.6894){
            if(isPressureIncreasingRapidly(readings, duration) > 0){ // rising or steady
                return "The weather will probably continue as is.";
            }
            else if(isPressureIncreasingRapidly(readings, duration) == -1){ // slowly falling
                return "The weather will probably continue as is.";
            }
            else { // rapidly falling
                return "It is probably going to rain (or snow).";
            }
        }
        else{
            if(isPressureIncreasingRapidly(readings, duration) >= 0){ // rising or steady
                return "The weather will probably be clearer and cooler";
            }
            else if(isPressureIncreasingRapidly(readings, duration) == -1){ // slowly falling
                return "It is probably going to rain (or snow).";
            }
            else { // rapidly falling
                return "There will probably be a storm.";
            }
        }
    }

    /**
     * Simple function to check if pressure is increasing rappidly or not
     * @param readings pressure readings
     * @return 1: increasing, -1: decreasing, 0: no major change
     */
    private static int isPressureIncreasingRapidly(float[] readings, int duration){

        float dif = readings[0] - readings[readings.length - 1];
        if(dif < 0){ // increase
            if(duration >= 60)
                return 2; // rapid
            else
                return 1; // slowly
        }
        else if(dif > 0){
            if(duration >= 60)
                return -2; // rapid
            else
                return -1; // slowly
        }
        else return 0; // no major change

    }

    private static float[] transformToFloatArray(String data){

        String[] tokens = data.split(";");
        float[] readings = new float[tokens.length];
        for(int i = 0; i < tokens.length; i++) {
            readings[i] = Float.parseFloat(tokens[i]);
        }
        return  readings;
    }

}

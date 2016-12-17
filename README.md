# Band API

The Beftigre Android (Band) API is for the mobile tier measurement.
Band API is a jar file which can be applied to an android project through gradle build file (_build.gradle_) .

See [beftigre-mca.appspot.com](http://beftigre-mca.appspot.com/) for Band Documentation.

This project was packaged using the [Android Studio IDE](https://developer.android.com/studio).


### Running the API

#### 1. Installation
Beftigre’s BAND API, is bundled as BeftigreAND.jar, and have been tested on android studio, as it is the recommended/standardised android IDE. To install BeftigreAND.jar:

 * Copy the jar file to the project’s libs folder. The final library path would be

    ```
    [ProjectName]/app/libs/BeftigreAND.jar
    ```

 * Add the file as a dependency (alongside other libraries) in the application’s build.gradle file located at
    `[ProjectName]/app/build.gradle` as shown in the snippet below.

    ```java
    ...
    dependencies {
        compile 'com.android.support:support-v4:18.0.0'
        androidTestCompile 'com.jayway.android.robotium:robotium-solo:5.4.1'
        compile files('libs/BeftigreAND.jar')
    }
    ```

Furthermore, Band API depends on two sets of configuration settings within the application’s manifest file located at `[ProjectName]/app/src/main/AndroidManifest.xml`.
 The configuration settings are:

 * Set the required Manifest permissions as shown in the snippet below.
      * The power monitoring feature requires _location_, _network_, _Wi-Fi_, _internet_, _phone_ and _boot_ permissions.
      * The data logging feature requires _external storage_ permission.
      <br/><br/>

    ```xml
    <?xml version="1.0" encoding="utf-8"?>
    <manifest xmlns:android="http://schemas.android.com/apk/res/android" package="sample.package">
        <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
        <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
        <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
        <uses-permission android:name="android.permission.INTERNET" />
        <uses-permission android:name="android.permission.READ_PHONE_STATE" />
        <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
        <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
        ...
    ```

 * Set the required Manifest services as shown in the snippet below.<br>
   Within the application tag UMLoggerService android service is required for power monitoring component.
   Also the BaseService android service is required for computing the    mobile % CPU and memory availability
   (i.e. actual Given clause), when getBaseStatus() API method is called.

    ```xml
        ...
        <application android:icon="@drawable/icon" android:label="@string/name">
            <service android:name="com.beftigre.band.mon.UMLoggerService" />
            <service android:name="com.beftigre.band.mon.BaseService" />
        </application>
    </manifest>
    ```


#### 2. Initialise API variables

The first step to using Band in the Test module is initialisation of API variables. (See section 5, for using Band within Application module). The initialisation step is achieved in two folds, as shown below:

 * Instantiating the Band and Marker objects

    ```java
    import com.beftigre.band.Band;
    import com.beftigre.band.Marker;
    import com.beftigre.band.annotations.*;

    public class SampleTest extends...{
        private Band band;
        private Marker m = new Marker("Label");

        public SampleTest(){
            super(SampleActivity.class);
        }
        ...
    ```

* Starting power monitoring and registering markers in setUp method


    ```java
        ...
        @Override
        protected void setUp() throws Exception {
            super.setUp();
            band = new Band(getActivity(), this);
            band.startPowerMonitoring();
            band.registerMarkers(m);
        }
        ...
    ```


#### 3. Write the test logic: Evaluation vs. Comparison

This step involves using annotations on the test method, and using the start and finish markers to specifies the code segments being tested.<br/>
Evaluation and Comparison are treated as two different concepts in the Beftigre Framework.
Using Band API, both concepts can be achieved depending on the way annotations are specified within the test project.


##### Using Band for Evaluation

Evaluation in Beftigre is the process of evaluating an MCA or offloading scheme by itself (otherwise called self-evaluation).

 * An evaluation can be done with different environmental conditions.
 * Evaluation is achieved by setting the attributes of the @Given annotation to 0, and ignoring the @When and @Then annotations.

    ```java
        ...
        @Given(mobileCPU=0, mobileMemory=0)
        public void testMethod() throws Exception {
            m.start();
            /*do test*/
            m.finish();
        }
        ...
    ```


##### Using Band for Comparison

Comparison in Beftigre is the process of comparing two different offloading schemes against each other, based on common environmental conditions.

 * The environmental condition used for the expected annotations has to be similar to that of the actual test process.
 * A comparison is accomplished by setting the values of all annotations (@Given, @When and @Then) to the result values of an offloading scheme, being compared against the scheme under test.
   Where the scheme under test is the offloading scheme currently being evaluated for comparison.

    ```java
        ...
        @Given(mobileCPU=97, mobileMemory=26)
        @When (bandwidth=4387, latency=31, cloudCPU=42, cloudMemory=20)
        @Then (mElapsedTime=21832, mUsedEnergy=721.3, cUsedCPU=58, cUsedMemory=30)
        public void testMethod() throws Exception {
            m.start();
            /*do test*/
            m.finish();
        }
        ...
    ```


#### 4. Finalise the test

This step involves saving the markers, stopping the power monitor and collecting idle resource usage data.

<ul>
    <li>
<pre><code class="language-java">
    ...
    @Override
    protected void tearDown() throws Exception {
        band.saveMarkers();
        band.stopPowerMonitoring();
        band.getBaseStatus();
        super.tearDown();
    }
}
</code></pre>
    </li>
</ul>


#### 5. Using Band In Application Code

The snippet below illustrates how to use Band in the application module. (See description above for usage in Test module). 

<ul>
    <li>
<pre><code class="language-java">
import com.beftigre.band.Band;
import com.beftigre.band.Marker;
import com.beftigre.band.exceptions.*;

public class SampleActivity extends...{
    private Band band = new Band();
    private Marker m1 = new Marker("Label1");
    private Marker mN = new Marker("LabelN");

    @Override
    protected void onCreate(Bundle...)() {
        super.onCreate(savedInstanceState);
        //start power monitor from test class
        try{
            band.registerMarkers(m1, mN);
        }catch(DuplicateLabelException d){...}
        /*app code*/
    }

    public void appMethod1() throws Exception {
        m1.start();
        /*app code*/
        m1.finish();
    }

    public void appMethodN() throws Exception {
        mN.start();
        /*app code*/
        mN.finish();
    }

    //save markers within test class
}
</code></pre>
    </li>
</ul>


### Dependencies

 * [JDK v1.8.0_112 x64](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
 * [PowerTutor Model](https://play.google.com/store/apps/details?id=edu.umich.PowerTutor)
 * [Android Gradle v2.2.3](https://developer.android.com/studio/releases/gradle-plugin.html)
 * Used SDK versions:
    * `compileSdkVersion 25`
    * `buildToolsVersion 25.0.2`
    * `minSdkVersion 15`
    * `targetSdkVersion 25`


### Package Structure

The package structure of Band API is as follows: com.beftigre.band...

 * `annotations`: contains annotation interfaces for full-tier annotations.

    * Given
    * Then
    * When

 * `exceptions`: contains the classes for exception handling.

    * DuplicateFinishMarkerException
    * DuplicateLabelException
    * DuplicateStartMarkerException
    * UnevenMarkersException

 * `mon`: contains the packages for PowerTutor monitoring, and custom classes for logging and persistence.

    * `components`
    * `phone`
    * `service`
    * `util`
    * BaseService
    * Persistence
    * UMLoggerService

 * Band: is a class which represents the core entry point for the API.
 It exposes methods used to setup and complete the test process.

 * Marker: is a class which implements objects for marking the start and finish points of the mobile test.
 It makes use of the interfaces from the annotations package.


### Author

Samuel Chinenyeze <sjchinenyeze@gmail.com>


### License

[MIT license](http://opensource.org/licenses/MIT)
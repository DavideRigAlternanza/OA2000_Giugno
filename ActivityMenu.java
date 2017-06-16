package it.oa2000;


import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

import java.util.ArrayList;

public class ActivityMenu extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int REQUEST_CODE_CAPTURE_IMAGE = 1;
    private static final int REQUEST_CODE_CREATOR = 2;
    private static final int REQUEST_CODE_RESOLUTION = 3;

    GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        final ListView listViewInterventi = (ListView) findViewById(R.id.listViewInterventi);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        final DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        //Azzero la selezione e inizializzo la lettura
        Properties.getInstance().selInt = 0;
        Properties.getInstance().lett = new CLettura();


        //Effettuo la lettura
        Properties.getInstance().lett.CreateFile("elenco_interventi.csv","XPIF");
        Properties.getInstance().lett.read("elenco_interventi.csv","XPIF");


        ArrayList<String> lista = new ArrayList<String>();
        lista = fillRecordArray(lista, -1, null);


        //Adattatore di dati da Lista di oggetti a lista di layout
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, lista);
        listViewInterventi.setAdapter(adapter);


        //Faccio qualcosa con la toolbar (?)
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();


        //Listener della navigationView
        navigationView.setNavigationItemSelectedListener(this);


        //Listener del floating action button
        fab.setOnClickListener(new View.OnClickListener()
                               {
                                   @Override
                                   public void onClick(View view) {
                                       //loadFile("elenco_interventi.xlsx");
                                       Intent openInputActivity = new Intent(ActivityMenu.this, ActivityRapporto.class);
                                       startActivity(openInputActivity);
                                   }
                               }
        );

        // Listener della listViewInterventi
        listViewInterventi.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adattatore, final View componente, int pos, long id){

                ArrayList<String> lista = new ArrayList<String>();
                lista = fillRecordArray(lista, pos, adattatore);
                listViewInterventi.setAdapter( new ArrayAdapter<String>(ActivityMenu.this, android.R.layout.simple_list_item_1, lista) );

            }
        });


    }

    public void loadFile(String filename) {
        // Create a query for a specific filename in Drive.
        Query query = new Query.Builder()
                .addFilter(Filters.eq(SearchableField.TITLE, filename))
                .build();
        // Invoke the query asynchronously with a callback method
        Drive.DriveApi.query(mGoogleApiClient, query)
                .setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
                    @Override
                    public void onResult(DriveApi.MetadataBufferResult result) {
                        // T E M P
                    }
                });
    }

    public ArrayList<String> fillRecordArray(ArrayList<String> lista, final int pos, final AdapterView<?> adattatore){

        String temp;
        for (int i = 0; i < Properties.getInstance().lett.numInterventi(); ++i) {
            CRecord record = Properties.getInstance().lett.getRecord(i);
            if(i != pos)
                temp = getClosedSelection(record);
            else {
                temp = getOpenSelection(record);
                if(temp.equals(adattatore.getItemAtPosition(pos).toString())) {      // Nel caso l'oggeto sia già aperto
                    temp = getClosedSelection(record);
                }
            }
            lista.add(temp);
        }
        return lista;

    }

    public String getOpenSelection(CRecord record){
        String temp = record.getValoreInd(CRecord.NCHIAMATA) + " " +
                record.getValoreInd(CRecord.CLIENTE) + "\n" +
                record.getValoreInd(CRecord.DATA) + "\n" +
                record.getValoreInd(CRecord.CONTATTO) + "\n" +
                record.getValoreInd(CRecord.RICHIESTACLIENTE) + "\n" +
                record.getValoreInd(CRecord.PRESSO);

        return temp;
    }

    public String getClosedSelection(CRecord record){
        String temp =  record.getValoreInd(CRecord.NCHIAMATA) +
                " " + record.getValoreInd(CRecord.CLIENTE);
        return temp;
    }



    @Override
    protected void onStart() {
        super.onStart();
        //mGoogleApiClient.connect();

        //Caricamento immagine profilo
        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        final ImageView headerImageView = (ImageView)navigationView.getHeaderView(0).findViewById(R.id.headerImageView);
        final TextView txtViewBenvenuto = (TextView)navigationView.getHeaderView(0).findViewById(R.id.HeaderMenuTxtViewBenvenuto);
        final TextView txtViewMail = (TextView)navigationView.getHeaderView(0).findViewById(R.id.HeaderMenuTxtViewMail);

        final SharedPreferences sharedPref = getSharedPreferences("sPref", 0);
        final SharedPreferences userPref = getSharedPreferences( sharedPref.getString("userLogged","") + "Pref", 0 );

        updateImage(userPref, headerImageView);

        //Inserisco il nome personalizzato nell'header della siderbar
        String tempStr1 = userPref.getString("nome", "");
        String tempStr2 = userPref.getString("username", "");
        if(!tempStr1.equals(""))
            txtViewBenvenuto.setText("Benvenuto " + tempStr1);
        else
            txtViewBenvenuto.setText("Benvenuto " + tempStr2);

        //Inserisco la mail personalizzata nell'header della sidebar
        String tempStr3 = userPref.getString("mail", "");
        txtViewMail.setText(tempStr3);

    }
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
            } catch (IntentSender.SendIntentException e) {
                // Unable to resolve, message user appropriately
            }
        } else {
            GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
            apiAvailability.getErrorDialog(this, connectionResult.getErrorCode(), 0).show();
        }
    }
    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_RESOLUTION:
                if (resultCode == RESULT_OK) {
                    mGoogleApiClient.connect();
                }
                else
                    // Errore (?)
                    break;
        }
    }
    @Override
    public void onConnected(Bundle connectionHint) {
        int i = 7;

    }
    protected void onStop() {
        super.onStop();

        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }
    public void onConnectionSuspended(int cause) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            finish();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Aggiunge oggetti alla ActionBar
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();
        boolean newActivity = true, isLogout = false;
        Intent openInputActivity = new Intent();

        switch(id){
            case R.id.invia_rapporto:
                openInputActivity = new Intent(ActivityMenu.this, ActivityInvioFirme.class);
                break;
            case R.id.aggiungi_descrizione:
                openInputActivity = new Intent(ActivityMenu.this, DescrizioneIntervento.class);
                break;
            case R.id.spese_intervento:
                openInputActivity = new Intent(ActivityMenu.this, ActivityIntervento.class);
                break;
            case R.id.cerca_appuntamenti:
                newActivity = false;
                break;
            case R.id.cerca_colleghi:
                newActivity = false;
                break;
            case R.id.gestione_profilo:
                openInputActivity = new Intent(ActivityMenu.this, ActivityProfilo.class);
                break;
            case R.id.menu_logout:
                isLogout = true;
                doLogout();
                openInputActivity = new Intent(ActivityMenu.this, ActivityLogin.class);
                break;
            default:
                newActivity = false;
                break;
        }

        if(newActivity)
            startActivity(openInputActivity);

        if(isLogout)
            finish();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    public void updateImage(SharedPreferences userPref, ImageView headerImageView){

        String temp = userPref.getString("imagePath", "");

        if(!temp.equals(""))
            headerImageView.setImageURI(Uri.parse(temp));
        else
            headerImageView.setImageURI( Properties.getInstance().userIcoUri ); // Default

    }

    public void doLogout(){

        final SharedPreferences sharedPref = getSharedPreferences("sPref", 0);
        final SharedPreferences userPref = getSharedPreferences( sharedPref.getString("userLogged","") + "Pref", 0 );

        SharedPreferences.Editor userEditor = userPref.edit();
        SharedPreferences.Editor sEditor = sharedPref.edit();
        userEditor.putBoolean("isLogged", false);
        sEditor.putString("userLogged", "");
        userEditor.commit();
        sEditor.commit();

    }
}

// esta clase extiende de LiveData dado que esta clase nos permite controlar cuando 
//el ciclo de vida de la actividad esta en estado activo o no 
// para ello podemos sobre escribir los metodos onActive() y OnInactive()
// en estos metodos lo que se recomienda es poder sacar el oyente cuando la aplicacion este 
//en estado stop para evitar que reciba notificaciones que pueden probocar perdida en la memoria
//en esta clase se notifica al observador colocando el valor de la variable que envuelve este live data
//se usa el metodo setValue(dato), al colocar esto se notifica al observador pasando como parametro la variable 
//dato para que el observador pueda manejar esto 


public class FirebaseQueryLiveData extends LiveData<DataSnapshot> {
    private static final String LOG_TAG = "FirebaseQueryLiveData";

    private final Query query;
    private final MyValueEventListener listener = new MyValueEventListener();
    private boolean listenerRemovePending = false;
    private final Handler handler = new Handler();
    
    // tarea para desactivar un oyente a la base de datos 
    
    
    private final Runnable removeListener = new Runnable() {
    @Override
    public void run() {
        query.removeEventListener(listener);
        listenerRemovePending = false;
    }
};
    
  //constructor que recibe una query como parametro
    public FirebaseQueryLiveData(Query query) {
        this.query = query;
    }
   
   //constructor que recibe una referencia de la base de datos 
    public FirebaseQueryLiveData(DatabaseReference ref) {
        this.query = ref;
    }

    @Override
    protected void onActive() {
        Log.d(LOG_TAG, "onActive");
         if (listenerRemovePending) {
        handler.removeCallbacks(removeListener);
    }
    else {
        query.addValueEventListener(listener);
    }
    listenerRemovePending = false;
    }

    @Override
    protected void onInactive() {
        Log.d(LOG_TAG, "onInactive");
        // Listener removal is schedule on a two second delay
    handler.postDelayed(removeListener, 2000);
    listenerRemovePending = true;
    }

    private class listener implements ValueEventListener {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            setValue(dataSnapshot);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.e(LOG_TAG, "Can't listen to query " + query, databaseError.toException());
        }
    }
}

// esta clase contiene como atributo un LiveData que es de la clase FireBasequeryLiveData
//ademas tiene un metodo que devuelve a este atributo que es necesario en la activity
//para poder obtener este atributo y añadirle un observador 


public class HotStockViewModel extends ViewModel {
    private static final DatabaseReference HOT_STOCK_REF =
        FirebaseDatabase.getInstance().getReference("/hotstock");

    private final FirebaseQueryLiveData liveData = new FirebaseQueryLiveData(HOT_STOCK_REF);

    @NonNull
    public LiveData<DataSnapshot> getDataSnapshotLiveData() {
        return liveData;
    }
}


// codigo para la actividad principal

HotStockViewModel viewModel = ViewModelProviders.of(this).get(HotStockViewModel.class);
LiveData<DataSnapshot> liveData = viewModel.getDataSnapshotLiveData();

liveData.observe(this, new Observer<DataSnapshot>() {
    @Override
    public void onChanged(@Nullable DataSnapshot dataSnapshot) {
        if (dataSnapshot != null) {
            //actualizo la ui con los valores del datasnapshot
            // update the UI here with values in the snapshot
        }
    }
});





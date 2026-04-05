import { configureStore } from "@reduxjs/toolkit";
import { assetReducer } from "./AssetReducer";
import { errorReducer } from "./errorReducer";

import { authReducer } from "./authReducer";


const user = localStorage.getItem("auth")
    ? JSON.parse(localStorage.getItem("auth"))
    : null;




const initialState = {
    auth: { user: user },
   
};

export const store = configureStore({
    reducer: {
       // products: assetReducer,
         assets : assetReducer,
        errors: errorReducer,
       
        auth: authReducer,
      // payment: paymentMethodReducer,
    },
    preloadedState: initialState,
});

export default store;
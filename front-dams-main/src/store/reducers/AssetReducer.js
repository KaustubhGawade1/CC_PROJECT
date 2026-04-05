const initialState = {
    assets: [],
    //categories: null,
    //pagination: {},
};

export const assetReducer = (state = initialState, action) => {
    switch (action.type) {
        case "FETCH_ASSETS":
            return {
                ...state,
                assets: action.payload,
                // pagination: {
                //     ...state.pagination,
                //     pageNumber: action.pageNumber,
                //     pageSize: action.pageSize,
                //     totalElements: action.totalElements,
                //     totalPages: action.totalPages,
                //     lastPage: action.lastPage,
                // },
            };

        // case "FETCH_PRODUCTS":
        //     return {
        //         ...state,
        //         assets: action.payload,
        //         // pagination: {
        //         //     ...state.pagination,
        //         //     pageNumber: action.pageNumber,
        //         //     pageSize: action.pageSize,
        //         //     totalElements: action.totalElements,
        //         //     totalPages: action.totalPages,
        //         //     lastPage: action.lastPage,
        //         // },
        //     };
    
      
        
    
        default:
            return state;
    }
};
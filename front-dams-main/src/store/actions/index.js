import api from "../../api/api";
import axios from "axios";

export const fetchAssets = () => async (dispatch) => {
  try {
    dispatch({ type: "IS_FETCHING" });
    const { data } = await api.get(`/public/asset`);
    console.log("Assets fetched:", data);

    dispatch({
      type: "FETCH_ASSETS",
      payload: data,
    });

    dispatch({ type: "IS_SUCCESS" });
  } catch (error) {
    console.log(error);
    dispatch({
      type: "IS_ERROR",
      payload: error?.response?.data?.message || "Failed to fetch assets",
    });
  }
};

export const authenticateSignInUser = (sendData, toast, reset, navigate, setLoader) => async (dispatch) => {
    try {
        setLoader(true);

        const { data } = await api.post("/auth/signin", sendData, { withCredentials: true })


        dispatch({ type: "LOGIN_USER", payload: data });

        // Extract pure token if jwtToken exists
        if (data.jwtToken) {
            let pureToken = data.jwtToken;
            if (pureToken.includes("=")) {
                pureToken = pureToken.split("=")[1].split(";")[0];
            }
            localStorage.setItem("authToken", pureToken);
        }

        // Save the entire user data object separately for profile info etc.
        localStorage.setItem("auth", JSON.stringify(data));

        reset();
        toast.success("Login Success");
        navigate("/");
    } catch (error) {
        console.error(error);
        toast.error(error?.response?.data?.message || "Internal Server Error");
    } finally {
        setLoader(false);
    }
};




export const registerNewUser 
    = (sendData, toast, reset, navigate, setLoader) => async (dispatch) => {
        try {
            setLoader(true);
            const { data } = await api.post("/auth/signup", sendData);
            reset();
            toast.success(data?.message || "User Registered Successfully");
            navigate("/login");
        } catch (error) {
            console.log(error);
            toast.error(error?.response?.data?.message || error?.response?.data?.password || "Internal Server Error");
        } finally {
            setLoader(false);
        }
};


export const logOutUser = (navigate) => async (dispatch) => {
  try {
    // Call backend signout API "to clear JWT cookie
    await api.post("/auth/signout", {}, {
      withCredentials: true, // important to send cookies
    });

    // Dispatch logout action to Redux
    dispatch({ type: "LOG_OUT" });

    // Remove auth data from localStorage
    localStorage.removeItem("auth");

    // Navigate to login page
    navigate("/login");

  } catch (error) {
    console.error("Error logging out:", error);
  }
};


export const handleDeleteAsset = async () => {
  if (window.confirm("Are you sure you want to delete this asset?")) {
    try {
      await api.delete(`/asset/${asset_id}`);
      alert("Asset deleted successfully");

      // Optionally refresh asset list here or lift state up to parent to remove deleted item from UI
      window.location.reload(); // quick reload approach
    } catch (error) {
      console.error("Delete error:", error);
      alert("Failed to delete asset");
    }
  }
};


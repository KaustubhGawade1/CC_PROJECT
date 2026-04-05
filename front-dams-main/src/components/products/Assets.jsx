import { FaExclamationTriangle, FaFolderOpen } from "react-icons/fa";
import ProductCard from "../shared/ProductCard";
import { useDispatch, useSelector } from "react-redux";
import { useEffect } from "react";
import { fetchAssets } from "../../store/actions";
import Loader from "../shared/Loader";
// import Paginations from "../shared/Paginations";

const Assets = () => {
  const { isLoading, errorMessage } = useSelector((state) => state.errors);
  const { assets } = useSelector((state) => state.assets);
  const dispatch = useDispatch();

  useEffect(() => {
    dispatch(fetchAssets());
  }, [dispatch]);

  return (
    <div className="lg:px-14 sm:px-8 px-4 py-14 2xl:w-[90%] 2xl:mx-auto">
      <h1 className="text-4xl font-bold text-center mb-12 text-slate-800">
        <FaFolderOpen className="inline-block mb-2 mr-2 text-blue-600" />
        All Assets
      </h1>

      {isLoading ? (
        <Loader />
      ) : errorMessage ? (
        <div className="flex justify-center items-center h-[200px]">
          <FaExclamationTriangle className="text-red-600 text-3xl mr-2" />
          <span className="text-slate-800 text-lg font-medium">
            {errorMessage}
          </span>
        </div>
      ) : (
        <div className="min-h-[700px]">
          {assets && assets.length > 0 ? (
            <div className="pb-6 pt-4 grid 2xl:grid-cols-4 lg:grid-cols-3 sm:grid-cols-2 gap-y-10 gap-x-8">
              {assets.map((item, i) => (
                <ProductCard key={item.asset_id || i} {...item} />
              ))}
            </div>
          ) : (
            <div className="flex justify-center items-center h-[200px]">
              <span className="text-slate-600 text-lg">
                No assets found.
              </span>
            </div>
          )}

          {/* Pagination if needed */}
          {/* <div className="flex justify-center pt-10">
            <Paginations
              numberOfPage={pagination?.totalPages}
              totalAssets={pagination?.totalElements}
            />
          </div> */}
        </div>
      )}
    </div>
  );
};

export default Assets;

import React, { useState } from "react";
import axios from "axios";
import { FaCloudUploadAlt } from "react-icons/fa";

const AddAsset = () => {
  const [file, setFile] = useState(null);
  const [uploading, setUploading] = useState(false);
  const [message, setMessage] = useState("");
  const authData = JSON.parse(localStorage.getItem("auth"));
  const token = authData?.jwtToken;

  // Clean jwtToken if it contains cookie attributes
  let pureToken = token;
  if (token?.includes("=")) {
    pureToken = token.split("=")[1].split(";")[0];
  }

  const handleFileChange = (e) => {
    setFile(e.target.files[0]);
  };

  const handleUpload = async (e) => {
    e.preventDefault();

    if (!file) {
      setMessage("Please select a file first.");
      return;
    }

    const formData = new FormData();
    formData.append("file", file);

    try {
      setUploading(true);
      setMessage("");

      const response = await axios.post(
        `${import.meta.env.VITE_BACK_END_URL}/api/asset/add/file`,
        formData,
        {
          headers: {
            "Content-Type": "multipart/form-data",
            Authorization: `Bearer ${pureToken}`,
          },
        }
      );

      console.log("Upload response:", response.data);
      setMessage("✅ Asset uploaded successfully!");
      setFile(null);
    } catch (error) {
      console.error("Upload error:", error);
      setMessage("❌ Failed to upload asset.");
    } finally {
      setUploading(false);
    }
  };

  return (
    <div className="max-w-md mx-auto mt-10 p-8 bg-gradient-to-br from-white to-gray-100 rounded-xl shadow-lg">
      <h1 className="text-3xl font-bold text-center mb-6 text-blue-700">
        <FaCloudUploadAlt className="inline-block mr-2 mb-1" />
        Add New Asset
      </h1>

      <form onSubmit={handleUpload} className="flex flex-col gap-5">
        <input
          type="file"
          onChange={handleFileChange}
          className="border-2 border-dashed border-gray-300 p-3 rounded-md text-gray-700 focus:outline-none focus:border-blue-500 transition"
        />

        <button
          type="submit"
          disabled={uploading}
          className={`py-2 px-4 rounded-md text-white font-semibold transition transform hover:scale-105 ${
            uploading
              ? "bg-gray-400 cursor-not-allowed"
              : "bg-gradient-to-r from-blue-500 to-purple-500 hover:from-blue-600 hover:to-purple-600"
          }`}
        >
          {uploading ? "Uploading..." : "Upload Asset"}
        </button>
      </form>

      {message && (
        <p className="mt-6 text-center text-lg font-medium text-gray-700">
          {message}
        </p>
      )}
    </div>
  );
};

export default AddAsset;

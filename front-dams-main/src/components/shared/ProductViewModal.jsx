import { Dialog, DialogBackdrop, DialogPanel, DialogTitle } from '@headlessui/react';
import { Divider } from '@mui/material';
import { MdClose, MdDone } from 'react-icons/md';

function ProductViewModal({ open, setOpen, product }) {
  // Guard in case product is undefined during initial render
  if (!product) return null;

  const {
    asset_id,
    filename,
    contentType,
    size,
    filePath,
    uploadedAt,
    uploadedBy
  } = product;

  return (
    <Dialog
      open={open}
      as="div"
      className="relative z-10"
      onClose={() => setOpen(false)}
    >
      <DialogBackdrop className="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity" />
      <div className="fixed inset-0 z-10 w-screen overflow-y-auto">
        <div className="flex min-h-full items-center justify-center p-4">
          <DialogPanel className="relative transform overflow-hidden rounded-lg bg-white shadow-xl transition-all md:max-w-[620px] md:min-w-[620px] w-full">

            {/* Image or preview */}
            {contentType.includes("image") && filePath && (
              <div className="flex justify-center aspect-[3/2]">
                <img
                  src={`${import.meta.env.VITE_BACK_END_URL}/${filePath}`}
                  alt={filename}
                  className="object-cover"
                />
              </div>
            )}

            <div className="px-6 pt-10 pb-2">
              <DialogTitle as="h1" className="lg:text-3xl sm:text-2xl text-xl font-semibold leading-6 text-gray-800 mb-4">
                {filename}
              </DialogTitle>

              <div className="space-y-2 text-gray-700 pb-4">
                <div className="flex items-center justify-between gap-2">
                  <span className="text-xl font-bold">
                    Size: {(Number(size) / 1024).toFixed(2)} KB
                  </span>

                  <div className="flex items-center gap-2">
                    <MdDone className="text-teal-900" />
                    <span className="bg-teal-200 text-teal-900 px-2 py-1 rounded-md text-xs">Available</span>
                  </div>
                </div>

                <Divider />

                <p>Content Type: {contentType}</p>
                <p>Uploaded By: {uploadedBy?.username}</p>
                <p>Uploaded At: {new Date(uploadedAt).toLocaleString()}</p>
              </div>
            </div>

            <div className="px-6 py-4 flex justify-end gap-4">
              <button
                onClick={() => setOpen(false)}
                type="button"
                className="px-4 py-2 text-sm font-semibold text-slate-700 border border-slate-700 hover:text-slate-800 hover:border-slate-800 rounded-md"
              >
                Close
              </button>
            </div>

          </DialogPanel>
        </div>
      </div>
    </Dialog>
  );
}

export default ProductViewModal;

export const formatsize = (amount) => {
 return new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: "USD",
 }).format(amount);
}


export const formatsizeCalculation = (quantity, size) => {
   return (Number(quantity) * Number(size)).toFixed(2);
  }
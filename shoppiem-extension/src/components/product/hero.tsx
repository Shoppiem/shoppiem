import React, {FC} from "react";

const Hero: FC<any> =  (_) => {

  return (
      <div>
        <div className="disclaimer">
          Shoppiem is currently in Beta. If you would like to get in touch with us, please send us an email at help@shoppiem.com.
        </div>
        <div className="break-2"></div>
        <div className="section-header">
          Shoppiem is a virtual assistant that helps you save time and money by answering your Amazon product questions instantly.
        </div>
        <div className="break-2"></div>
        <div className="landing-section-header">
          💡<span>Open an Amazon product page to use Shoppiem</span>
        </div>
        <div className="break-1"></div>
        <div className="landing-section">
          Shoppiem combines data on the specific product you are interested in from Amazon and all over the Internet to provide you with accurate information.
        </div>
        <div className="break-1"></div>
        <div className="landing-section-header">
          💡<span>Shoppiem is powered by AI</span>
        </div>
        <div className="break-1"></div>
        <div className="landing-section">
          When using Shoppiem, ask detailed questions about the product and use keywords. For example, say "How long does the battery last on this laptop?" or "How many liters of water can I carry in this bottle?"
        </div>
      </div>
  );
}
export default Hero
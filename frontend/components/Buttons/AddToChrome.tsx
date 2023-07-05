import Link from "next/link";

const AddToChrome = ({hidden = false}: {hidden?: boolean }) => {
  let className = "py-3 px-7 text-base font-bold text-dark hover:opacity-70 dark:text-white md:block"
  if (hidden) {
    className = "hidden py-3 px-7 text-base font-bold text-dark hover:opacity-70 dark:text-white md:block"
  }

  return (
      <div
          className="wow fadeInUp mb-12 max-w-[570px] lg:mb-0"
          data-wow-delay=".15s"
      >
        <div className="mx-[-12px] flex flex-wrap">
          <Link
              href="https://chrome.google.com/webstore/detail/shoppiem-chatgpt-for-amaz/hbmifofekjlighdmkcjaciljlkibfojc/related"
              className={className}
          >
            <div className="chrome-button">
              <svg width="100%" height="100%" viewBox="0 0 16 16"
                   xmlns="http://www.w3.org/2000/svg" className="index_icon__sjqiJ">
                <g fillRule="nonzero" fill="none">
                  <path
                      d="M7.98.001s4.716-.211 7.216 4.52H7.578S6.14 4.474 4.913 6.216c-.354.73-.73 1.483-.306 2.966-.614-1.035-3.255-5.626-3.255-5.626S3.215.19 7.979.001"
                      fill="#E14C40"></path>
                  <path
                      d="M14.95 12.015s-2.175 4.183-7.53 3.978l3.81-6.585s.76-1.22-.138-3.151c-.456-.671-.921-1.374-2.42-1.749 1.206-.012 6.509.001 6.509.001s1.988 3.294-.23 7.506"
                      fill="#FFD24D"></path>
                  <path
                      d="M1.042 12.047s-2.542-3.972.315-8.499l3.807 6.587s.678 1.267 2.803 1.456c.81-.059 1.652-.109 2.727-1.217C10.1 11.422 7.438 16 7.438 16s-3.852.072-6.396-3.953"
                      fill="#00AA60"></path>
                  <path
                      d="M4.39 8.065a3.566 3.566 0 0 1 3.57-3.562 3.566 3.566 0 0 1 3.568 3.562 3.566 3.566 0 0 1-3.569 3.563A3.566 3.566 0 0 1 4.39 8.065"
                      fill="#FFF"></path>
                  <path
                      d="M5.16 8.065a2.796 2.796 0 0 1 2.8-2.793 2.796 2.796 0 0 1 2.798 2.793 2.796 2.796 0 0 1-2.799 2.794 2.796 2.796 0 0 1-2.798-2.794"
                      fill="#577FC0"></path>
                  <path
                      d="M7.979.001s3.114-.14 5.623 2.346c-.042 0-8.566.027-11.305.027.99-.99 2.809-2.259 5.682-2.373"
                      fill="#D6483E"></path>
                </g>
              </svg>
              Add to Chrome
            </div>
          </Link>
        </div>
      </div>
  );
};

export default AddToChrome;

import { ButtonHTMLAttributes } from "react";

type ButtonProps = ButtonHTMLAttributes<HTMLButtonElement>;

export function Button({ className = "", type, ...props }: ButtonProps) {
  return (
    <button
      type={type ?? "button"}
      className={`inline-flex items-center justify-center gap-2 rounded-full px-4 text-sm font-medium transition-colors focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#F2F0FA] disabled:pointer-events-none ${className}`}
      {...props}
    />
  );
}

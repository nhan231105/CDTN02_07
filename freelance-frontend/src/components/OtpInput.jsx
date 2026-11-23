import { Field } from './UI'

export default function OtpInput({ value, onChange }) {
  return (
    <Field
      id="otp"
      label="Mã xác minh"
      hint="Nhập 6 chữ số trong email. Mã có hiệu lực 5 phút."
    >
      <input
        id="otp"
        inputMode="numeric"
        autoComplete="one-time-code"
        pattern="[0-9]{6}"
        maxLength={6}
        required
        value={value}
        onChange={(event) => onChange(event.target.value.replace(/\D/g, ''))}
      />
    </Field>
  )
}

import { ContentSection } from '../components/content-section'
import { AppearanceForm } from './appearance-form'

export function SettingsAppearance() {
  return (
    <ContentSection
      title='Giao diện'
      desc='Tùy chỉnh giao diện bảng điều khiển. Tự động chuyển đổi giữa chủ đề sáng và tối.'
    >
      <AppearanceForm />
    </ContentSection>
  )
}

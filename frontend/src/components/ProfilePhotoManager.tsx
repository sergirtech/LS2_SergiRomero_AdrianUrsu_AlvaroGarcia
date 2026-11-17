import { useState, type ChangeEvent } from 'react';

interface Props {
  photoUrl: string | null;
  loading: boolean;
  saving: boolean;
  error: string | null;
  onUpload: (file: File) => Promise<boolean>;
  onDelete: () => Promise<boolean>;
}

const ProfilePhotoManager = ({ photoUrl, loading, saving, error, onUpload, onDelete }: Props) => {
  const [fileInputKey, setFileInputKey] = useState(0);

  const selectPhoto = async (event: ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file) return;
    await onUpload(file);
    setFileInputKey((key) => key + 1);
  };

  return (
    <div className="pt-profile-photo-manager">
      <label htmlFor={`profile-photo-${fileInputKey}`}>Profile photo (PNG, JPG, JPEG, SVG; max 5 MB)</label>
      <input
        key={fileInputKey}
        id={`profile-photo-${fileInputKey}`}
        type="file"
        accept=".png,.jpg,.jpeg,.svg,image/png,image/jpeg,image/svg+xml"
        onChange={(event) => void selectPhoto(event)}
        disabled={saving}
      />
      {photoUrl && (
        <button type="button" onClick={() => void onDelete()} disabled={saving || loading}>
          Remove profile photo
        </button>
      )}
      {error && <p role="alert">{error}</p>}
    </div>
  );
};

export default ProfilePhotoManager;
